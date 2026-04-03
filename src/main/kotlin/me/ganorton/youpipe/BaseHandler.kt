package me.ganorton.youpipe

import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

public abstract class BaseHandler(protected val basePath: String, protected val templateBase: String? = null) : Handler<RoutingContext> {
	public open val defaultTab: String? = null
	public open val tabHandlers: Map<String, Tab> = mapOf()
	public open val supportHandlers: Map<String, (RoutingContext) -> Unit> = mapOf()

	private val templateRoot: String get() = this.templateBase ?: this.basePath

	public open fun attachTo(router: Router): BaseHandler {
		val endpointBase = this.basePath

		router.route(endpointBase).handler { ctx ->
			var tab = ctx.queryParams()["tab"]
			if (tab == null || !this.isFragment(ctx)) {
				ctx.data<String>().put("pageTemplate", this.templateRoot)
				this.handle(ctx)
			}

			if (tab == null || !(tab in this.tabHandlers)) {
				tab = this.defaultTab
			}
			this.initTab(ctx, tab, this.tabHandlers[tab]!!)
			this.tabHandlers[tab]?.handler(ctx)
			ctx.next()
		}

		for ((supportName, supportHandler) in this.supportHandlers) {
			router.route("$endpointBase/$supportName").handler { ctx ->
				supportHandler(ctx)
				ctx.next()
			}
		}
		return this
	}

	public fun isFragment(ctx: RoutingContext): Boolean {
		return ctx.request().getHeader("HX-Request") != null
	}

	protected open fun initTab(ctx: RoutingContext, tabName: String, tab: Tab) {
		ctx.data<String>().put("tabTemplate", "${this.templateRoot}/$tabName")
		/*if (displayUrl != null) {
			if (!this.isFragment(ctx)) {
				ctx.redirect(displayUrl)
			} else {
				ctx.data<String>().put("hxPushUrl", displayUrl)
			}
		}
		if (template != null) {
			ctx.data<String>().put("tabTemplate", template)
		}*/
	}

	public data class Tab(val handler: (RoutingContext) -> Unit)
}
