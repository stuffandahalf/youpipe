// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.handlers

import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.RoutingContext
import java.io.File
import java.io.FileOutputStream
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.schabi.newpipe.extractor.Extractor
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamExtractor
import me.ganorton.youpipe.PageHandler
import me.ganorton.youpipe.RouteChangeOptions

public class VideoHandler(basePath: String) : PageHandler("$basePath/:id", basePath) {
	public override val defaultTab = "comments"
	public override val tabHandlers: Array<PageHandler.Tab> = arrayOf(
		PageHandler.Tab("Comments", "comments", ::handleComments),
		PageHandler.Tab("Related", "related", ::handleRelated),
		PageHandler.Tab("Description", "description", ::handleDescription))

	public override val supportHandlers: Map<String, (RoutingContext) -> Unit> = mapOf(
		"stream" to ::handleStream,
		"player" to ::handlePlayer)

	protected override fun setup(ctx: RoutingContext) {
		super.setup(ctx)

		var streamExtractor = ctx.data<StreamExtractor>()["extractor"]
		if (streamExtractor != null) {
			return
		}

		val service = YoutubeService(0)
		val linkHandler = YoutubeStreamLinkHandlerFactory.getInstance()
		streamExtractor = service.getStreamExtractor(linkHandler.getUrl(ctx.data<String>()["id"]))
		streamExtractor.fetchPage()
		streamExtractor.getDescription()
		ctx.data<StreamExtractor>().put("extractor", streamExtractor)
	}

	public override fun handle(ctx: RoutingContext) {
		ctx.data<Boolean>().put("primaryEndpoint", true)
		this.handlePlayer(ctx)
	}

	public fun handlePlayer(ctx: RoutingContext) {
		val extractor = ctx.data<StreamExtractor>()["extractor"]

		val streamList = arrayOf(
			extractor!!.getVideoStreams()
				.map { StreamOption(it.getResolution(), true, it.getFormat()?.getMimeType() ?: "video/mp4", it.getContent()) },
			extractor!!.getVideoOnlyStreams()
				.filter { it.getCodec() == "vp9" }
				.map { StreamOption(it.getResolution(), false, "video/mp4", it.getContent()) }).flatMap { it }
		ctx.data<List<StreamOption>>().put("streamList", streamList)

		var selectedStream = streamList[0]
		val selectedStreamEncoded = ctx.request().getParam("stream")
		if (selectedStreamEncoded != null) {
			selectedStream = Json.decodeFromString<StreamOption>(selectedStreamEncoded.replace('\'', '"'))
		} else {
			val quality = ctx.queryParam("q").getOrNull(0)
			val direct = ctx.queryParam("d").getOrNull(0)?.toBoolean() ?: quality == null
			println("quality=$quality, direct=$direct")
			var stream: StreamOption? = null
			if (quality != null) {
				stream = streamList.find { it.quality == quality && it.direct == direct }
			}
			if (stream != null) {
				selectedStream = stream
			}
		}
		ctx.data<StreamOption>().put("selectedStream", selectedStream)
		ctx.data<String>().put("thumbnailUrl", extractor.getThumbnails().sortedBy { it.getEstimatedResolutionLevel() }.getOrNull(0)?.getUrl() ?: "")

		if (ctx.data<Boolean>()["primaryEndpoint"] != true) {
			ctx.data<String>().put("pageTemplate", "watch/player")
			ctx.data<RouteChangeOptions>().put("urlUpdateOptions",
				RouteChangeOptions(route="${ctx.data<String>()["basePath"]}?q=${selectedStream.quality}&d=${selectedStream.direct}", updateMethod="HX-Replace-Url"))
		}

		println("SELECTED STREAM = $selectedStream")
	}

	private fun cleanupFfmpeg(ctx: RoutingContext, process: Process?) {
		if (process == null) {
			return
		}

		val session = ctx.session()
		val sessionProcess: Process? = session["activeStream"]

		process.destroy()
		if (process.equals(sessionProcess)) {
			session.put("activeStream", null)
		}
		println("FFMPEG KILLED")
	}

	/* TODO: find a way to return metadata from ffmpeg stream */
	/* TODO: look into fixing all of the concurrency issues here */
	public fun handleStream(ctx: RoutingContext) {
		val extractor = ctx.data<StreamExtractor>()["extractor"]
		val session = ctx.session()

		var process: Process? = session["activeStream"]
		this.cleanupFfmpeg(ctx, process)

		/*for (stream in extractor!!.getVideoOnlyStreams()) {
			println("VIDEO STREAM ${stream.getResolution()}, ${stream.getWidth()} x ${stream.getHeight()}, ${stream.getBitrate()}, ${stream.getCodec()}, ${stream.getContent()}")
		}
		for (stream in extractor!!.getAudioStreams()) {
			println("AUDIO STREAM ${stream.getQuality()} - ${stream.getBitrate()} - ${stream.getCodec()} - ${stream.getContent()}")
		}*/

		val vquality = ctx.queryParam("q").getOrNull(0) ?: "720p"
		val validStreams = extractor!!.getVideoOnlyStreams().filter { it.getCodec() == "vp9" }
		val vstream = validStreams.find { it.getResolution() == vquality } ?: validStreams.last()
		val astream = extractor!!.getAudioStreams().getOrNull(0)

		//val chunksz = 512
		//val chunksz = 4096
		val chunksz = 16384
		val buffer = ByteArray(chunksz)

		// need to check isUrl of streams to confirm if getContent is a url
		val errorLog = File("error.log")
		//val processBuilder = ProcessBuilder("ffmpeg", "-i", vstream.getContent(), "-i", astream.getContent(), "-c:v", "copy", "-c:a", "copy", "-f", "mp4", "-movflags", "frag_keyframe+empty_moov", "-").redirectError(errorLog)
		val processBuilder = ProcessBuilder("ffmpeg", "-i", vstream?.getContent() ?: "", "-i", astream?.getContent() ?: "", "-c:v", "copy", "-c:a", "copy", "-f", "mp4", "-movflags", "+frag_keyframe+empty_moov+default_base_moof", "-").redirectError(errorLog)
		//val processBuilder = ProcessBuilder("ffmpeg", "-i", vstream.getContent(), "-i", astream.getContent(), "-listen", "1", "-c:v", "copy", "-c:a", "copy", "-f", "mp4", "-movflags", "frag_keyframe+empty_moov", "http://localhost:8889").redirectError(errorLog)
		process = processBuilder.start()
		session.put("activeStream", process)
		val output = process.getInputStream()

		//println("PLAYING ${vstream.getResolution()} - ${astream.getQuality()} (duration ${extractor.getLength()})")


		ctx.response().closeHandler {
			this.cleanupFfmpeg(ctx, process)
		}
		ctx.response().setChunked(true)
		ctx.response().headers()["Content-Type"] = "video/mp4"
		//ctx.response().headers()["Content-Length"] = "${(vstream.getBitrate() + astream.getBitrate()) * extractor.getLength()}"
		//ctx.response().headers()["Accept-Ranges"] = "bytes"

		/*var exitCode = process.waitFor()
		if (exitCode != 0) {
			System.err.println("SOMETHING WENT WRONG")
		}*/
		//val exitCode = process.waitFor()

		/*val outFile = File("output.mp4")
		outFile.createNewFile()
		val outStream = FileOutputStream(outFile)*/
		var outCount = 0
		while ({ outCount = output.read(buffer); outCount }() >= 0) {
			//println("READ OUT ($outcount) = $buffer")
			//outStream.write(buffer, 0, outcount)
			val outBuffer = Buffer.buffer().appendBytes(buffer, 0, outCount)
			ctx.response().write(outBuffer).await()
		}
		//ctx.redirect("http://localhost:8889")
		val exitCode = process.waitFor()
		this.cleanupFfmpeg(ctx, process)
		//ctx.response().sendFile("output.mp4")
		println("FFMPEG EXITED $exitCode")
		ctx.end()
	}

	public fun handleComments(ctx: RoutingContext) {
		val id = ctx.pathParam("id")
		println("VIDEO ID (COMMENTS) $id")
	}

	public fun handleRelated(ctx: RoutingContext) {
		val id = ctx.pathParam("id")
		val extractor = ctx.data<StreamExtractor>()["extractor"]

		val related = extractor?.getRelatedItems()
		ctx.data<List<InfoItem>>().put("listItems", related?.getItems() ?: listOf<InfoItem>())
	}

	public fun handleDescription(ctx: RoutingContext) {
		val id = ctx.pathParam("id")

		val extractor = ctx.data<StreamExtractor>()["extractor"]
		ctx.data<String>().put("description", extractor?.getDescription()?.getContent() ?: "")
	}

	@Serializable
	public data class StreamOption private constructor(val quality: String, val direct: Boolean, val mimeType: String) {
		public var url: String = ""
			private set

		public constructor(quality: String, direct: Boolean, mimeType: String, url: String) : this(quality, direct, mimeType) {
			this.url = url
		}

		public fun toJson(): String = Json.encodeToString(this).replace('"', '\'')
	}
}
