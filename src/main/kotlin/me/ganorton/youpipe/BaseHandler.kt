package me.ganorton.youpipe

import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

public abstract class BaseHandler(protected val basePath: String) : Handler<RoutingContext> {
	public abstract fun attachTo(router: Router): BaseHandler

	public fun isFragment(ctx: RoutingContext): Boolean {
		return ctx.request().getHeader("HX-Request") != null
	}
}
