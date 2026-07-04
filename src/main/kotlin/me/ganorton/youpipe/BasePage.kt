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
import me.ganorton.youpipe.RouteChangeOptions

public abstract class BasePage(public val basePath: String, public val templateBase: String? = null) : Handler<RoutingContext> {
	protected val service = YoutubeService(0)

	public open val defaultTab: String? = null
	public open val tabs: Array<TabHandler> = arrayOf()
	public open val supportHandlers: Map<String, (RoutingContext) -> Unit> = mapOf()

	protected open fun filterTab(ctx: RoutingContext, tab: TabHandler): Boolean = true

	private fun mainHandler(ctx: RoutingContext) {
		this.setup(ctx)
		ctx.data<String>().put("pageTemplate", this.templatePrefix)
		ctx.data<Iterable<TabHandler>>().put("tabList", this.tabs.filter { this.filterTab(ctx, it) })
		this.handle(ctx)
	}

	public fun attachTo(router: Router): BasePage {
		router.route(this.basePath).handler { ctx ->
			try {
				this.mainHandler(ctx)
				val tab = ctx.data<String>()["activeTab"] ?: this.defaultTab
				if (this.tabs.size > 0 && tab != null) {
					ctx.reroute("${ctx.request().path()}/${this.defaultTab}")
				} else {
					ctx.next()
				}
			} catch (e: Exception) {
				println(e);
				ctx.data<Exception>().put("exception", e)
				ctx.reroute("/error")
			}
		}
		if (this.tabs.size > 0) {
			router.route("${this.basePath}/:tab").handler { ctx ->
				try {
					val tab = ctx.pathParam("tab")
					ctx.data<String>().put("activeTab", tab)
					if (!this.isFragment(ctx)) {
						this.mainHandler(ctx)
					} else {
						this.setup(ctx)
					}
					ctx.next()
				} catch (e: Exception) {
					println(e);
					ctx.data<Exception>().put("exception", e)
					ctx.reroute("/error")
				}
			}
			for (tabHandler in this.tabs) {
				//tabHandler.attachTo(router)
				router.route("${this.basePath}/${tabHandler.tabName}").handler { ctx ->
					this.setup(ctx)
					tabHandler.handler(ctx)
					ctx.data<String>().put("tabTemplate", "${this.templatePrefix}/${tabHandler.tabName}")
					ctx.next()
				}
			}
		}

		for ((supportName, supportHandler) in this.supportHandlers) {
			router.route("${this.basePath}/$supportName").handler { ctx ->
				/* Don't need to push url for support endpoints */
				ctx.data<RouteChangeOptions>().put("urlUpdateOptions", RouteChangeOptions(null, null))

				try {
					setup(ctx)
					supportHandler(ctx)
					if (!ctx.response().ended()) {
						ctx.next()
					}
				} catch (e: Exception) {
					ctx.data<Exception>().put("exception", e)
					ctx.reroute("/error")
				}
			}
		}
		return this
	}

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

	public data class TabHandler(val tabName: String, val handler: (RoutingContext) -> Unit)
}

public data class PaginationContext(val basePath: String, val extractor: ListExtractor<InfoItem>, val metadata: Map<String, Any>?, var pageNum: Int, var nextPage: Page?)
