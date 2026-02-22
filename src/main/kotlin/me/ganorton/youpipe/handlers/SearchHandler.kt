package me.ganorton.youpipe.handlers

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory

public class SearchHandler : Handler<RoutingContext> {
	public override fun handle(ctx: RoutingContext) {
		val service = YoutubeService(0)

		val queryParam = ctx.queryParams().get("query") ?: ""
		ctx.data<String>().put("query", queryParam)
		if (queryParam.equals("")) {
			ctx.next()
			return@handle
		}

		val query = YoutubeSearchQueryHandlerFactory
			.getInstance()
			.fromQuery(
				queryParam,
				listOf(YoutubeSearchQueryHandlerFactory.ALL), null)
		val searchExtractor = service.getSearchExtractor(query)
		searchExtractor.fetchPage()
		val page = searchExtractor.getInitialPage()
		for (item in page.getItems()) {
			println("[%s] %s (%s)".format(item.getInfoType(), item.name, item.url))
		}
		ctx.data<List<InfoItem>>().put("items", page.getItems())
		ctx.data<String>().put("nextPage", page.getNextPage()?.url)
		ctx.next()
	}
}
