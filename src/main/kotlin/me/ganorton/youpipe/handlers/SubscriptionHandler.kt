// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.handlers

import io.vertx.ext.web.RoutingContext
import me.ganorton.youpipe.PageHandler
import me.ganorton.youpipe.managers.SubscriptionManager
import me.ganorton.youpipe.utilities.FileUtility

public class SubscriptionHandler(basePath: String, subscriptionsPath: String) : PageHandler(basePath) {
	public override val supportHandlers: Map<String, (RoutingContext) -> Unit> = mapOf(
		"import" to ::handleImport,
		"all" to ::handleAllSubscriptions)

	public override fun handle(ctx: RoutingContext) {
		println("SubscriptionHandler::handle")
		val form = ctx.request().formAttributes()
		println(form.names())
		//val subList = ctx.request().getFormAttribute("subscriptions")
		//println(subList)
	}

	public fun handleImport(ctx: RoutingContext) {
		println("SubscriptionHandler::handleImport")
		val importStrategy = ctx.request().getParam("importStrategy")
		val importedContents = ctx.fileUploads()
			.map { file ->
				val subs = SubscriptionManager.read(file.uploadedFileName())
				file.delete()
				subs
			}

		println("IMPORT ($importStrategy) = $importedContents")

		//println("CURRENT = ${this.data}")
		// TODO add these subs to manager

		ctx.redirect(this.basePath)
	}

	public fun handleAllSubscriptions(ctx: RoutingContext) {
		println("SubscriptionHandler::handleAllSubscriptions")
	}

	public fun handleAdd(ctx: RoutingContext) {
		println("SubscriptionHandler::handleAdd")
	}

	public fun handleRemove(ctx: RoutingContext) {
		println("SubscriptionHandler::handleRemove")
	}
}

