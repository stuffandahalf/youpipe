// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.handlers

import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import me.ganorton.youpipe.PageHandler

public class SubscriptionHandler(basePath: String) : PageHandler(basePath) {
	public override val supportHandlers: Map<String, (RoutingContext) -> Unit> = mapOf("import" to ::handleImport)

	public override fun handle(ctx: RoutingContext) {
		println("SubscriptionHandler::handle")
		val form = ctx.request().formAttributes()
		println(form.names())
		//val subList = ctx.request().getFormAttribute("subscriptions")
		//println(subList)
	}

	public fun handleImport(ctx: RoutingContext) {
		println("SubscriptionHandler::handleImport")
		ctx.next()
	}
}
