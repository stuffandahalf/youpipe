package me.ganorton.youpipe.handlers

import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import me.ganorton.youpipe.BaseHandler

public class SubscriptionHandler(basePath: String) : BaseHandler(basePath) {
	public override fun attachTo(router: Router): BaseHandler {
		router.route(this.basePath).handler(::handle)
		router.route(this.basePath + "/import").handler(::handleImport)
		return this
	}

	public override fun handle(ctx: RoutingContext) {
		println("SubscriptionHandler::handle")
		val form = ctx.request().formAttributes()
		println(form.names())
		//val subList = ctx.request().getFormAttribute("subscriptions")
		//println(subList)
		ctx.next()
	}

	public fun handleImport(ctx: RoutingContext) {
		println("SubscriptionHandler::handleImport")
		ctx.next()
	}
}
