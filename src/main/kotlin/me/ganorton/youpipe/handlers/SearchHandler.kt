// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.handlers

import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import me.ganorton.youpipe.PageHandler

public class SearchHandler(basePath: String) : PageHandler(basePath) {
	public override fun handle(ctx: RoutingContext) {
		val service = YoutubeService(0)

		val queryParam = ctx.queryParams()["query"] ?: ""
		val pageNum = (ctx.queryParams()["page"] ?: "0").toInt()

		val session = ctx.session()
		var searchContext = session.get<SearchContext>("search")
		if (searchContext != null) {
			println(searchContext.toString())
		}

		ctx.data<Int>().put("pageNum", pageNum)
		if (queryParam.equals("")) {
			return
		}

		val pages = ArrayList<InfoItemsPage<InfoItem>>()
		if (searchContext == null || searchContext.query != queryParam || pageNum == 0 || pageNum <= searchContext.pageNum) {
			println("GETTING PAGE 0")
			val query = YoutubeSearchQueryHandlerFactory
				.getInstance()
				.fromQuery(
					queryParam,
					listOf(YoutubeSearchQueryHandlerFactory.ALL),
					null)
			val searchExtractor = service.getSearchExtractor(query)
			searchExtractor.fetchPage()
			val page = searchExtractor.getInitialPage()
			pages.add(page)
			searchContext = SearchContext(queryParam, searchExtractor, 0, page.getNextPage())
			session.put("search", searchContext)
		}
		val startPage = searchContext.pageNum + 1
		for (i in startPage..pageNum) {
			println("GETTING PAGE $i")
			val page = searchContext.extractor.getPage(searchContext.nextPage)
			pages.add(page)
			searchContext.pageNum = i
			searchContext.nextPage = page.getNextPage()
		}

		ctx.data<List<InfoItem>>().put("listItems", pages.flatMap { it.getItems() })
	}

	private data class SearchContext(val query: String, val extractor: SearchExtractor, var pageNum: Int, var nextPage: Page?)
}
