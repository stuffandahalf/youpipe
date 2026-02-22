package me.ganorton.youpipe.handlers

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory

public class SearchHandler : Handler<RoutingContext> {
	public override fun handle(ctx: RoutingContext) {
		val service = YoutubeService(0)
		//println("SEARCH \"%s\"".format(ctx.queryParam("query")))
		//ctx.response().end()
		/* TODO: check that at least one item is available to search for */
		val query = YoutubeSearchQueryHandlerFactory
			.getInstance()
			.fromQuery(
				ctx.queryParam("query").get(0),
				listOf(YoutubeSearchQueryHandlerFactory.ALL), null)
		val searchExtractor = service.getSearchExtractor(query)
		//println(searchExtractor.getSearchString())
		//println(searchExtractor.getUrl())
		searchExtractor.fetchPage()
		val page = searchExtractor.getInitialPage()
		for (item in page.getItems()) {
			println("[%s] %s (%s)".format(item.getInfoType(), item.name, item.url))
		}
		ctx.data<List<InfoItem>>().put("items", page.getItems())
		ctx.data<String>().put("nextPage", page.getNextPage()?.url)
		ctx.next()
		//ctx.response().end()
	}
}
