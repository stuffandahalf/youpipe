// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe

import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import me.ganorton.youpipe.RouteChangeOptions

public abstract class PageHandler(protected val basePath: String, protected val templateBase: String? = null) : Handler<RoutingContext> {
	public open val defaultTab: String? = null
	public open val tabHandlers: Array<Tab> = arrayOf()
	public open val supportHandlers: Map<String, (RoutingContext) -> Unit> = mapOf()

	private val templatePrefix: String get() {
		var p = this.templateBase ?: this.basePath
		if (p.startsWith('/')) {
			p = p.substring(1)
		}
		return p
	}

	protected open fun filterTab(ctx: RoutingContext, tab: Tab): Boolean = true

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
	}

	public open fun attachTo(router: Router): PageHandler {
		router.route(this.basePath).handler { ctx ->
			setup(ctx)

			var tab = ctx.queryParams()["tab"]
			if (tab == null || !this.isFragment(ctx)) {
				ctx.data<String>().put("pageTemplate", this.templatePrefix)
				this.handle(ctx)
			} else {
				ctx.data<RouteChangeOptions>()["urlUpdateOptions"]!!.updateMethod = "HX-Replace-Url"
			}

			val tabDef = this.tabHandlers.find { it.target == tab } ?:
				this.tabHandlers.find { it.target == this.defaultTab }
			if (tabDef != null) {
				ctx.data<Iterable<Tab>>().put("tabList", this.tabHandlers.filter { this.filterTab(ctx, it) })
				ctx.data<String>().put("tabTemplate", "${this.templatePrefix}/${tabDef.target}")
				tabDef.handler(ctx)
			}
			ctx.next()
		}

		for ((supportName, supportHandler) in this.supportHandlers) {
			router.route("${this.basePath}/$supportName").handler { ctx ->
				/* Don't need to push url for support endpoints */
				ctx.data<Boolean>().put("hxCancelPush", true)

				setup(ctx)
				supportHandler(ctx)
				ctx.next()
			}
		}
		return this
	}

	public fun isFragment(ctx: RoutingContext): Boolean {
		return ctx.request().getHeader("HX-Request") != null
	}

	public data class Tab(val name: String, val target: String, val handler: (RoutingContext) -> Unit)
}
