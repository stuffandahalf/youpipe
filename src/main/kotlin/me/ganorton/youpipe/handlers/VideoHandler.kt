// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.handlers

import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.Extractor
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
//import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamExtractor
import me.ganorton.youpipe.PageHandler

public class VideoHandler(basePath: String) : PageHandler("$basePath/:id", basePath) {
	public override val defaultTab = "comments"
	public override val tabHandlers: Array<PageHandler.Tab> = arrayOf(
		PageHandler.Tab("Comments", "comments", ::handleComments),
		PageHandler.Tab("Related", "related", ::handleRelated),
		PageHandler.Tab("Description", "description", ::handleDescription))

	public override val supportHandlers: Map<String, (RoutingContext) -> Unit> = mapOf("stream" to ::handleStream)

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

	public override fun handle(ctx: RoutingContext) {}

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

		val id = ctx.pathParam("id")
		val extractor = ctx.data<StreamExtractor>()["extractor"]

		val related = extractor?.getRelatedItems()
		ctx.data<List<InfoItem>>().put("listItems", related?.getItems() ?: listOf<InfoItem>())
	}

	public fun handleDescription(ctx: RoutingContext) {
		//val id = initTab(ctx, "description")
		val id = ctx.pathParam("id")

		val extractor = ctx.data<StreamExtractor>()["extractor"]
		ctx.data<String>().put("description", extractor?.getDescription()?.getContent() ?: "")
	}
}
