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
		val nextPage = ctx.queryParams()["next"] != null
		if (nextPage) {
			/* don't want to update url for paging */
			//ctx.response().headers().remove("HX-Push-Url")
		}
		var hxPushUrl = ctx.request().path()
		/*if (queryParam != "") {
		  // TODO: encode queryParam
		  hxPushUrl = hxPushUrl + "?query=" + queryParam
		}
		ctx.data<String>().put("hxPushUrl", hxPushUrl)*/

		/*val session = ctx.session()
		val searchContext = session.get<SearchContext>("search")
		if (searchContext != null) {
			println(searchContext.toString())
		}*/

		//ctx.data<String>().put("query", queryParam)
		ctx.data<Boolean>().put("nextPage", nextPage)
		if (queryParam.equals("")) {
			return
		}

		var page: InfoItemsPage<InfoItem>? = null
		//if (searchContext == null || searchContext.query != queryParam) {
			val query = YoutubeSearchQueryHandlerFactory
				.getInstance()
				.fromQuery(
					queryParam,
					listOf(YoutubeSearchQueryHandlerFactory.ALL),
					null)
			val searchExtractor = service.getSearchExtractor(query)
			searchExtractor.fetchPage()
			page = searchExtractor.getInitialPage()

			//session.put("search", SearchContext(queryParam, searchExtractor, page.getNextPage()))
		/*} else {
			page = searchContext.extractor.getPage(searchContext.nextPage)
			searchContext.nextPage = page.getNextPage()
			//session.put("search", SearchContext(queryParam, searchContext.extractor, page.getNextPage()))
		}*/

		if (page != null) {
			for (item in page.getItems()) {
				println("[%s] %s (%s)".format(item.getInfoType(), item.name, item.url))
			}

			ctx.data<List<InfoItem>>().put("items", page.getItems())
		}
	}

	private data class SearchContext(val query: String, val extractor: SearchExtractor, var nextPage: Page?)
}
