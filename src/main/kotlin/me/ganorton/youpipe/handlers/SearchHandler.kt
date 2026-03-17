package me.ganorton.youpipe.handlers

import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import me.ganorton.youpipe.BaseHandler

public class SearchHandler(basePath: String) : BaseHandler(basePath) {
	public override fun attachTo(router: Router): BaseHandler {
		router.route(this.basePath).handler(::handle)
		return this
	}

	public override fun handle(ctx: RoutingContext) {
		val service = YoutubeService(0)

		val queryParam = ctx.queryParams().get("query") ?: ""
		val nextPage = ctx.queryParams().get("next") != null
		if (nextPage) {
			/* don't want to update url for paging */
			//ctx.response().headers().remove("HX-Push-Url")
		}
		var hxPushUrl = ctx.request().path()
		if (queryParam != "") {
		  // TODO: encode queryParam
		  hxPushUrl = hxPushUrl + "?query=" + queryParam
		}
		ctx.data<String>().put("hxPushUrl", hxPushUrl)

		/*val session = ctx.session()
		val searchContext = session.get<SearchContext>("search")
		if (searchContext != null) {
			println(searchContext.toString())
		}*/

		ctx.data<String>().put("query", queryParam)
		ctx.data<Boolean>().put("nextPage", nextPage)
		if (queryParam.equals("")) {
			ctx.next()
			return@handle
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
		ctx.next()
	}

	private data class SearchContext(val query: String, val extractor: SearchExtractor, var nextPage: Page?)
}
