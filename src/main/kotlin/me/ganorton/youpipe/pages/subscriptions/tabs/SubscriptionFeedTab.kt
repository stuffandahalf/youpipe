// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages.subscriptions.tabs

import io.vertx.ext.web.RoutingContext
import java.time.Instant
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.local.subscription.workers.SubscriptionItem
import me.ganorton.youpipe.managers.SubscriptionManager
import me.ganorton.youpipe.pages.PageHandler
import me.ganorton.youpipe.pages.tabs.TabHandler

public class SubscriptionFeedTab(tabName: String, page: PageHandler) : TabHandler(tabName, page) {
	public override fun handle(ctx: RoutingContext) {
		println("SubscriptionHandler::handleAllSubscriptions")

		println("RESULT COUNT = ${SubscriptionManager.feed.size}, FAILURES = ${SubscriptionManager.feedFailures}")
		ctx.data<Instant>().put("feedLastUpdated", SubscriptionManager.feedLastUpdated)
		ctx.data<List<SubscriptionItem>>().put("failures", SubscriptionManager.feedFailures)
		ctx.data<List<StreamInfoItem>>().put("listItems", SubscriptionManager.feed)
	}
}

