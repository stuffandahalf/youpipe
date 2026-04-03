package me.ganorton.youpipe.handlers

import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.Extractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
//import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamExtractor
import me.ganorton.youpipe.BaseHandler

public class VideoHandler(basePath: String) : BaseHandler("$basePath/:id", basePath) {
	public override val defaultTab = "comments"
	public override val tabHandlers: Map<String, BaseHandler.Tab> = mapOf(
		"description" to BaseHandler.Tab(::handleDescription),
		"comments" to BaseHandler.Tab(::handleComments),
		"related" to BaseHandler.Tab(::handleRelated))

	public override val supportHandlers: Map<String, (RoutingContext) -> Unit> = mapOf("stream" to ::handleStream)

	private fun getStreamExtractor(ctx: RoutingContext, id: String): StreamExtractor {
		var streamExtractor = ctx.data<StreamExtractor>().get("extractor")
		if (streamExtractor != null) {
			return streamExtractor
		}

		val service = YoutubeService(0)
		val linkHandler = YoutubeStreamLinkHandlerFactory.getInstance()
		streamExtractor = service.getStreamExtractor(linkHandler.getUrl(id))
		streamExtractor.fetchPage()
		streamExtractor.getDescription()
		ctx.data<StreamExtractor>().put("extractor", streamExtractor)

		return streamExtractor
	}

	public override fun handle(ctx: RoutingContext) {
		val id = ctx.pathParam("id")
		ctx.data<String>().put("id", id)
		val streamExtractor = this.getStreamExtractor(ctx, id)
	}

	public fun handleStream(ctx: RoutingContext) {
		//ctx.end()
	}

	public fun handleComments(ctx: RoutingContext) {
		val id = ctx.pathParam("id")
		println("VIDEO ID (COMMENTS) $id")
	}

	public fun handleRelated(ctx: RoutingContext) {
		/*val id = initTab(ctx, "related")
		if (id == null) {
			return
		}
		println("VIDEO ID (RELATED) %s".format(id))*/
		//ctx.next()
	}

	public fun handleDescription(ctx: RoutingContext) {
		//val id = initTab(ctx, "description")
		val id = ctx.pathParam("id")

		val extractor = this.getStreamExtractor(ctx, id)
		ctx.data<String>().put("description", extractor.getDescription().getContent())
	}
}
