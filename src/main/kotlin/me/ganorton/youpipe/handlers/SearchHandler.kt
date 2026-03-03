package me.ganorton.youpipe.handlers

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory

public class SearchHandler : Handler<RoutingContext> {
	public override fun handle(ctx: RoutingContext) {
		val service = YoutubeService(0)

		val queryParam = ctx.queryParams().get("query") ?: ""
		val nextPage = ctx.queryParams().get("next") != null &&
			ctx.request().getHeader("HX-Request") != null

		val session = ctx.session()
		val searchContext = session.get<SearchContext>("search")
		if (searchContext != null) {
			println(searchContext.toString())
		}

		ctx.data<String>().put("query", queryParam)
		ctx.data<Boolean>().put("nextPage", nextPage)
		if (queryParam.equals("")) {
			ctx.next()
			return@handle
		}

		var page: InfoItemsPage<InfoItem>? = null
		if (searchContext == null || searchContext.query != queryParam) {
			val query = YoutubeSearchQueryHandlerFactory
				.getInstance()
				.fromQuery(
					queryParam,
					listOf(YoutubeSearchQueryHandlerFactory.ALL), null)
			val searchExtractor = service.getSearchExtractor(query)
			searchExtractor.fetchPage()
			page = searchExtractor.getInitialPage()

			session.put("search", SearchContext(queryParam, searchExtractor, page.getNextPage()))
		} else {
			page = searchContext.extractor.getPage(searchContext.nextPage)
		}

		if (page != null) {
			for (item in page.getItems()) {
				println("[%s] %s (%s)".format(item.getInfoType(), item.name, item.url))
			}

			ctx.data<List<InfoItem>>().put("items", page.getItems())
		}
		ctx.next()
	}

	private data class SearchContext(val query: String, val extractor: SearchExtractor, val nextPage: Page?)
}
