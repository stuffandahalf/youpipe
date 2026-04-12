// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.handlers

import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.local.subscription.workers.SubscriptionItem
import me.ganorton.youpipe.PageHandler
import me.ganorton.youpipe.managers.SubscriptionManager
import me.ganorton.youpipe.utilities.FileUtility

public class SubscriptionHandler(basePath: String, subscriptionsPath: String) : PageHandler(basePath) {
	public override val supportHandlers: Map<String, (RoutingContext) -> Unit> = mapOf(
		"import" to ::handleImport,
		"all" to ::handleAllSubscriptions)

	public override fun handle(ctx: RoutingContext) {
		ctx.data<List<SubscriptionItem>>().put("subscriptions", SubscriptionManager.data)
	}

	public fun handleImport(ctx: RoutingContext) {
		val importStrategy = ctx.request().getParam("importStrategy")
		val importedContents = ctx.fileUploads()
			.flatMap { file ->
				val subs = SubscriptionManager.read(file.uploadedFileName())
				file.delete()
				subs
			}

		SubscriptionManager.updateData(importStrategy, importedContents)
		ctx.redirect(this.basePath)
	}

	public fun handleAllSubscriptions(ctx: RoutingContext) {
		println("SubscriptionHandler::handleAllSubscriptions")
		ctx.data<String>().put("pageTemplate", "subscriptions/all")
	}

	public fun handleAdd(ctx: RoutingContext) {
		println("SubscriptionHandler::handleAdd")
	}

	public fun handleRemove(ctx: RoutingContext) {
		println("SubscriptionHandler::handleRemove")
	}
}

