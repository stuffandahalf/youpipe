// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe

import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.services.youtube.YoutubeService

public abstract class BaseHandler(public val basePath: String, public val templateBase: String? = null) : Handler<RoutingContext> {
	protected val service = YoutubeService(0)

	public abstract fun attachTo(router: Router): BaseHandler

	protected val templatePrefix: String get() {
		var p = this.templateBase ?: this.basePath
		if (p.startsWith('/')) {
			p = p.substring(1)
		}
		return p
	}

	protected open fun setup(ctx: RoutingContext) {
		val fragments = this.basePath.split('/')
		val params = fragments
			.filter { it.startsWith(':') }
			.map { it.substring(1) }
		for (param in params) {
			val v = ctx.pathParam(param)
			ctx.data<String>().put(param, v)
		}

		val rtBasePath = fragments
			.map { if (!it.startsWith(':')) it else ctx.data<String>()[it.substring(1)] }
			.joinToString(separator = "/")

		ctx.data<String>().put("basePath", rtBasePath)
		ctx.data<String?>().put("paginationPath", null)
	}

	/* utility handler for managing pagination context */
	protected fun paginationHandler(ctx: RoutingContext, metadata: Map<String, Any>?, buildExtractor: (ctx: RoutingContext) -> ListExtractor<InfoItem>?) {
		val pageNum = (ctx.queryParams()["page"] ?: "0").toInt()
		ctx.data<Int>().put("pageNum", pageNum)

		val session = ctx.session()
		var paginationContext = session.get<PaginationContext>("paginationContext")

		val pages = ArrayList<InfoItemsPage<InfoItem>>()
		if (paginationContext == null || paginationContext.basePath != ctx.data<String>()["basePath"] || pageNum == 0 || pageNum <= paginationContext.pageNum || paginationContext.metadata != metadata) {
			val extractor = buildExtractor(ctx)
			if (extractor == null) {
				return
			}
			paginationContext = PaginationContext(ctx.data<String>()["basePath"] ?: this.basePath, extractor, metadata, 0, null)
			println("GETTING PAGE 0")
			extractor.fetchPage()

			val page = extractor.getInitialPage()
			pages.add(page)
			paginationContext.nextPage = page.getNextPage()
			session.put("paginationContext", paginationContext)
		}
		println(paginationContext.toString())

		val startPage = paginationContext.pageNum + 1
		for (i in startPage..pageNum) {
			println("GETTING PAGE $i")
			val page = paginationContext.extractor.getPage(paginationContext.nextPage)
			pages.add(page)
			paginationContext.pageNum = i
			paginationContext.nextPage = page.getNextPage()
		}
		session.put("paginationContext", paginationContext)
		ctx.data<List<InfoItem>>().put("listItems", pages.flatMap { it.getItems() })
		ctx.data<PaginationContext>().put("paginationContext", paginationContext)
		ctx.data<String>().put("paginationPath", ctx.request().path())
	}
	public fun paginationHandler(ctx: RoutingContext, buildExtractor: (ctx: RoutingContext) -> ListExtractor<InfoItem>) = this.paginationHandler(ctx, null, buildExtractor)


	protected fun isFragment(ctx: RoutingContext): Boolean {
		return ctx.request().getHeader("HX-Request") != null
	}
}

public data class PaginationContext(val basePath: String, val extractor: ListExtractor<InfoItem>, val metadata: Map<String, Any>?, var pageNum: Int, var nextPage: Page?)
