// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages

import io.vertx.ext.web.RoutingContext
import java.io.FileInputStream
import java.time.Instant
import org.schabi.newpipe.extractor.feed.FeedExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.local.subscription.workers.SubscriptionItem
import me.ganorton.youpipe.BasePage
import me.ganorton.youpipe.managers.SettingsManager
import me.ganorton.youpipe.managers.SubscriptionManager

public class SubscriptionsPage(basePath: String, subscriptionsPath: String) : BasePage(basePath) {
	public override val preloadTabs = true
	public override val defaultTab = "list"
	public override val tabs = arrayOf(
		TabHandler("list", this::handleList),
		TabHandler("feed", this::handleFeed))

	public override val supportHandlers: Map<String, (RoutingContext) -> Unit> = mapOf(
		"import" to ::handleImport,
		"refresh" to ::handleRefreshFeed)

	public override fun handle(ctx: RoutingContext) { }

	public fun handleList(ctx: RoutingContext) {
		ctx.data<List<SubscriptionItem>>().put("subscriptions", SubscriptionManager.data)
		ctx.data<List<SubscriptionManager.ImportStrategy>>().put("importStrategies", SubscriptionManager.ImportStrategy.entries)
		ctx.data<List<SubscriptionManager.ExportSource>>().put("exportSources", SubscriptionManager.ExportSource.entries)
	}

	public fun handleFeed(ctx: RoutingContext) {
		println("SubscriptionHandler::handleAllSubscriptions")

		println("RESULT COUNT = ${SubscriptionManager.feed.size}, FAILURES = ${SubscriptionManager.feedFailures}")
		ctx.data<Instant>().put("feedLastUpdated", SubscriptionManager.feedLastUpdated)
		ctx.data<List<SubscriptionItem>>().put("failures", SubscriptionManager.feedFailures)
		ctx.data<List<StreamInfoItem>>().put("listItems", SubscriptionManager.feed)
	}

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
		ctx.redirect("$basePath?tab=feed")
	}

	public fun handleAdd(ctx: RoutingContext) {
		println("SubscriptionHandler::handleAdd")
	}

	public fun handleRemove(ctx: RoutingContext) {
		println("SubscriptionHandler::handleRemove")
	}
}

