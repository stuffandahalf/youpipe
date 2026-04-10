// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.handlers

import io.vertx.ext.web.RoutingContext
/*import kotlinx.serialization.Json
import kotlinx.serialization.Serializable*/
import me.ganorton.youpipe.DataPageHandler

public class SubscriptionHandler(basePath: String, subscriptionsPath: String) : DataPageHandler<Subscriptions>(subscriptionsPath, basePath) {
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
	}

	public fun handleAdd(ctx: RoutingContext) {
		println("SubscriptionHandler::handleAdd")
	}

	public fun handleRemove(ctx: RoutingContext) {
		println("SubscriptionHandler::handleRemove")
	}
}

//@Serializable
public data class Subscriptions(val tmp: String?)
