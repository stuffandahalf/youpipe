// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages.channel.tabs

import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import me.ganorton.youpipe.pages.PageHandler
import me.ganorton.youpipe.pages.tabs.TabHandler

public class ChannelTab(tab: String, page: PageHandler) : TabHandler(tab, page) {
	public override fun handle(ctx: RoutingContext) {
		val channelId = ctx.pathParam("channelId")
		this.paginationHandler(ctx) { ctx ->
			val linkHandler = this.service.getChannelTabLHFactory().fromQuery(channelId, listOf(this.tabName), "")
			val extractor = this.service.getChannelTabExtractor(linkHandler)
			extractor
		}
	}
}

