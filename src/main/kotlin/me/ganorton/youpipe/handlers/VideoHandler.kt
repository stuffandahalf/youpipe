package me.ganorton.youpipe.handlers

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.Extractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeStreamExtractor
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory

public class VideoHandler : Handler<RoutingContext> {
	public override fun handle(ctx: RoutingContext) {
		val id = ctx.queryParams().get("v") ?: ""
		if (id.equals("")) {
			ctx.next()
			return@handle
		}

		val service = YoutubeService(0)

		val linkHandler = YoutubeStreamLinkHandlerFactory.getInstance()
		val streamExtractor = service.getStreamExtractor(linkHandler.getUrl(id))
		streamExtractor.fetchPage()
		streamExtractor.getDescription()
		ctx.data<Extractor>().put("extractor", streamExtractor)

		ctx.next()
	}
}
