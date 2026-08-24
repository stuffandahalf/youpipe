// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages

import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import me.ganorton.youpipe.BasePage

public class SearchPage(basePath: String) : BasePage(basePath) {
	public override fun handle(ctx: RoutingContext) {
		val service = YoutubeService(0)

		val queryParam = ctx.queryParams()["query"] ?: ""
		ctx.data<String>().put("query", queryParam)

		this.paginationHandler(ctx, mapOf("query" to queryParam), {ctx: RoutingContext ->
			if (queryParam == "") {
				return@paginationHandler null
			}
			val query = YoutubeSearchQueryHandlerFactory
				.getInstance()
				.fromQuery(
					queryParam,
					listOf(YoutubeSearchQueryHandlerFactory.ALL),
					null)
			val searchExtractor = service.getSearchExtractor(query)
			searchExtractor
		})
	}
}
