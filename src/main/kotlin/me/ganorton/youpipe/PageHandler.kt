// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe

import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

public abstract class PageHandler(protected val basePath: String, protected val templateBase: String? = null) : Handler<RoutingContext> {
	public open val defaultTab: String? = null
	public open val tabHandlers: Map<String, Tab> = mapOf()
	public open val supportHandlers: Map<String, (RoutingContext) -> Unit> = mapOf()

	private val templateRoot: String get() = this.templateBase ?: this.basePath

	protected open fun setup(ctx: RoutingContext) {}

	public open fun attachTo(router: Router): PageHandler {
		val endpointBase = this.basePath

		router.route(endpointBase).handler { ctx ->
			setup(ctx)

			var tab = ctx.queryParams()["tab"]
			if (tab == null || !this.isFragment(ctx)) {
				ctx.data<String>().put("pageTemplate", this.templateRoot)
				this.handle(ctx)
			}

			if (tab == null || !(tab in this.tabHandlers)) {
				tab = this.defaultTab
			}
			if (tab != null) {
				ctx.data<String>().put("tabTemplate", "${this.templateRoot}/$tab")
				this.tabHandlers[tab]?.handler(ctx)
			}
			ctx.next()
		}

		for ((supportName, supportHandler) in this.supportHandlers) {
			router.route("$endpointBase/$supportName").handler { ctx ->
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

	public data class Tab(val handler: (RoutingContext) -> Unit)
}
