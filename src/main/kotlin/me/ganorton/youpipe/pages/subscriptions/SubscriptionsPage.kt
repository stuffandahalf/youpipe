// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages.subscriptions

import io.vertx.ext.web.RoutingContext
import java.io.FileInputStream
import org.schabi.newpipe.extractor.feed.FeedExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.local.subscription.workers.SubscriptionItem
import me.ganorton.youpipe.pages.PageHandler
import me.ganorton.youpipe.pages.subscriptions.tabs.SubscriptionListTab
import me.ganorton.youpipe.pages.subscriptions.tabs.SubscriptionFeedTab
import me.ganorton.youpipe.managers.SettingsManager
import me.ganorton.youpipe.managers.SubscriptionManager

public class SubscriptionsPage(basePath: String, subscriptionsPath: String) : PageHandler(basePath) {
	public override val defaultTab = "list"
	public override val tabs = arrayOf(
		SubscriptionListTab("list", this),
		SubscriptionFeedTab("feed", this))

	public override val supportHandlers: Map<String, (RoutingContext) -> Unit> = mapOf(
		"import" to ::handleImport,
		"refresh" to ::handleRefreshFeed)

	public override fun handle(ctx: RoutingContext) { }

	public fun handleImport(ctx: RoutingContext) {
		val importStrategy = ctx.request().getParam("importStrategy")
		val exportSource = ctx.request().getParam("exportSource")
		val importedContents = ctx.fileUploads()
			.flatMap { file ->
				val subs = FileInputStream(file.uploadedFileName()).use { SubscriptionManager.importStream(it, SubscriptionManager.ExportSource.valueOf(exportSource)) }
				file.delete()
				subs
			}

		SubscriptionManager.updateData(SubscriptionManager.ImportStrategy.valueOf(importStrategy), importedContents)
		ctx.redirect(this.basePath)
	}

	public fun handleRefreshFeed(ctx: RoutingContext) {
		println("SubscriptionHandler::handleRefreshFeed (THIS WILL TAKE A WHILE)")
		SubscriptionManager.retrieveFeed()
		ctx.redirect("$basePath/feed")
	}

	public fun handleAdd(ctx: RoutingContext) {
		println("SubscriptionHandler::handleAdd")
	}

	public fun handleRemove(ctx: RoutingContext) {
		println("SubscriptionHandler::handleRemove")
	}
}

