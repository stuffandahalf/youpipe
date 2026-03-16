package me.ganorton.youpipe.handlers

import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.Extractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory
import me.ganorton.youpipe.BaseHandler

public class VideoHandler : BaseHandler() {
	public override fun attachTo(router: Router, basePath: String): BaseHandler {
		router.route(basePath).handler(::handle)
		router.route(basePath + "/:id/stream").handler(::handleStream)
		router.route(basePath + "/:id/description").handler(::handleDescription)
		router.route(basePath + "/:id/comments").handler(::handleComments)
		router.route(basePath + "/:id/related").handler(::handleRelated)
		return this
	}

	public override fun handle(ctx: RoutingContext) {
		val session = ctx.session()

		val id = ctx.queryParams().get("v") ?: ""
		if (id.equals("")) {
			ctx.next()
			return@handle
		}
		val tab = ctx.queryParams().get("tab") ?: "comments"
		val target = ctx.request().getHeader("HX-Target")
		if (target != null) {
			ctx.reroute("/watch/" + tab)
			return
		}

		val service = YoutubeService(0)

		val linkHandler = YoutubeStreamLinkHandlerFactory.getInstance()
		val streamExtractor = service.getStreamExtractor(linkHandler.getUrl(id))
		streamExtractor.fetchPage()
		streamExtractor.getDescription()

		ctx.data<String>().put("id", id)
		ctx.data<Extractor>().put("extractor", streamExtractor)
		session.put("extractor", streamExtractor)

		ctx.data<String>().put("pageTemplate", "/watch")
		when (tab) {
			"comments" -> this.handleComments(ctx)
			"related" -> this.handleRelated(ctx)
			"description" -> this.handleDescription(ctx)
			else -> ctx.next()
		}
	}

	public fun handleStream(ctx: RoutingContext) {
		ctx.end()
	}

	public fun handleComments(ctx: RoutingContext) {
		val template = "/watch/comments"
		if (ctx.data<String>().get("pageTemplate") == null) {
			ctx.data<String>().put("pageTemplate", template)
		} else {
			ctx.data<String>().put("tabTemplate", template)
		}

		var id = ctx.data<String>().get("id")
		if (id == null) {
			id = ctx.pathParam("id")
			ctx.data<String>().put("id", id)
		}
		println("VIDEO ID".format(id))
    
		val service = YoutubeService(0)

		ctx.next()
	}

	public fun handleRelated(ctx: RoutingContext) {
		ctx.data<String>().put("tabTemplate", "/watch/related")
		ctx.next()
	}

	public fun handleDescription(ctx: RoutingContext) {
		ctx.data<String>().put("tabTemplate", "/watch/description")

		val session = ctx.session()

		val streamExtractor = session.get<YoutubeStreamExtractor>("extractor")
		ctx.data<String>().put("description", streamExtractor.getDescription().getContent())

		ctx.next()
	}
}
