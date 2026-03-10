package me.ganorton.youpipe

import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

public abstract class BaseHandler : Handler<RoutingContext> {
	public abstract fun attachTo(router: Router, basePath: String): BaseHandler
}
