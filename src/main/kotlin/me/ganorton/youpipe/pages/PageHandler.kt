// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages

import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import me.ganorton.youpipe.BaseHandler
import me.ganorton.youpipe.RouteChangeOptions
import me.ganorton.youpipe.pages.tabs.TabHandler
import me.ganorton.youpipe.utilities.FileUtility

public abstract class PageHandler(basePath: String, templateBase: String? = null) : BaseHandler(basePath, templateBase) {
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

	public override fun attachTo(router: Router): PageHandler {
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
					}
					ctx.next()
				} catch (e: Exception) {
					println(e);
					ctx.data<Exception>().put("exception", e)
					ctx.reroute("/error")
				}
			}
			for (tabHandler in this.tabs) {
				tabHandler.attachTo(router)
			}
		}

		for ((supportName, supportHandler) in this.supportHandlers) {
			router.route("${this.basePath}/$supportName").handler { ctx ->
				/* Don't need to push url for support endpoints */
				ctx.data<Boolean>().put("hxCancelPush", true)

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
}
