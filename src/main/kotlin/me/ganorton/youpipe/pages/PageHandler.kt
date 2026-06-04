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

	public override fun attachTo(router: Router): PageHandler {
		router.route(this.basePath).handler { ctx ->
			try {
				this.setup(ctx)

				ctx.data<String>().put("pageTemplate", this.templatePrefix)
				ctx.data<Iterable<TabHandler>>().put("tabList", this.tabs.filter { this.filterTab(ctx, it) })
				this.handle(ctx)
				if (this.defaultTab != null && ctx.data<Boolean>()["fromTab"] != true) {
					ctx.reroute("${ctx.request().path()}/${this.defaultTab}")
				}
			} catch (e: Exception) {
				println(e);
				ctx.data<Exception>().put("exception", e)
				ctx.reroute("/error")
			}
			if (!ctx.response().ended() && ctx.data<Boolean>()["fromTab"] != true) {
				ctx.next()
			}
		}
		if (this.tabs.size > 0) {
			for (tabHandler in this.tabs) {
				tabHandler.attachTo(router)
			}
			router.route("${this.basePath}/:tab").handler { ctx ->
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
