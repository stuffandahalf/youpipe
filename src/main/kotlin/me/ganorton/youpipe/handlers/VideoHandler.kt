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

public class VideoHandler(basePath: String) : BaseHandler(basePath) {
	public override fun attachTo(router: Router): BaseHandler {
		router.route(this.basePath).handler(::handle)
		router.route(this.basePath + "/:id/stream").handler(::handleStream)
		router.route(this.basePath + "/:id/description").handler(::handleDescription)
		router.route(this.basePath + "/:id/comments").handler(::handleComments)
		router.route(this.basePath + "/:id/related").handler(::handleRelated)
		return this
	}

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
		val id = ctx.queryParams().get("v") ?: ""
		if (id.equals("")) {
			ctx.next()
			return@handle
		}
		ctx.data<String>().put("id", id)

		val tab = ctx.queryParams().get("tab") ?: "comments"
		val target = ctx.request().getHeader("HX-Target")
		if (target != null) {
			ctx.reroute("/watch/" + tab)
			return
		}

		val streamExtractor = this.getStreamExtractor(ctx, id)

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

	private fun initTab(ctx: RoutingContext, tab: String): String? {
		val template = this.basePath + "/" + tab
		ctx.data<String>().put("tabTemplate", template)

		var id = ctx.data<String>().get("id")
		if (id == null) {
			id = ctx.pathParam("id")
			ctx.data<String>().put("id", id)

			// only set pushUrl if not called from page
			val pushUrl = "%s?v=%s&tab=%s".format(this.basePath, id, tab)
			ctx.data<String>().put("hxPushUrl", pushUrl)
		}
		val redirectUrl = ctx.data<String>().get("hxPushUrl")
		if (!this.isFragment(ctx) && redirectUrl != null) {
			ctx.redirect(redirectUrl)
			return null
		}
		return id
	}

	public fun handleComments(ctx: RoutingContext) {
		val id = initTab(ctx, "comments")
		if (id == null) {
			return
		}
		println("VIDEO ID (COMMENTS) %s".format(id))

		ctx.next()
	}

	public fun handleRelated(ctx: RoutingContext) {
		val id = initTab(ctx, "related")
		if (id == null) {
			return
		}
		println("VIDEO ID (RELATED) %s".format(id))
		ctx.next()
	}

	public fun handleDescription(ctx: RoutingContext) {
		val id = initTab(ctx, "description")
		if (id == null) {
			return
		}
		ctx.data<String>().put("tabTemplate", "/watch/description")
		println("VIDEO ID (DESCRIPTION) %s".format(id))

		val extractor = this.getStreamExtractor(ctx, id)
		ctx.data<String>().put("description", extractor.getDescription().getContent())

		ctx.next()
	}
}
