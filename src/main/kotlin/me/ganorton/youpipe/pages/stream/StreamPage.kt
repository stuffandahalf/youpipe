// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages.stream

import io.vertx.ext.web.RoutingContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamExtractor
import me.ganorton.youpipe.pages.PageHandler
import me.ganorton.youpipe.pages.tabs.TabHandler

public class StreamPage(basePath: String) : PageHandler("$basePath/:id", basePath) {
	//public override val tabs = arrayOf()

	protected override fun setup(ctx: RoutingContext) {
		super.setup(ctx)

		var streamExtractor = ctx.data<StreamExtractor>()["extractor"]
		if (streamExtractor != null) {
			return
		}

		val linkHandler = YoutubeStreamLinkHandlerFactory.getInstance()
		streamExtractor = this.service.getStreamExtractor(linkHandler.getUrl(ctx.data<String>()["id"]))
		streamExtractor.fetchPage()
		streamExtractor.getDescription()
		ctx.data<StreamExtractor>().put("extractor", streamExtractor)
	}

	public override fun handle(ctx: RoutingContext) {
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
			/*ctx.data<RouteChangeOptions>().put("urlUpdateOptions",
				RouteChangeOptions(route="${ctx.data<String>()["basePath"]}?q=${selectedStream.quality}&d=${selectedStream.direct}", updateMethod="HX-Replace-Url"))*/
		}

		println("SELECTED STREAM = $selectedStream")
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

