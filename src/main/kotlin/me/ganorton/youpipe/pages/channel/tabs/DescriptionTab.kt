// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages.channel.tabs

import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import me.ganorton.youpipe.pages.PageHandler
import me.ganorton.youpipe.pages.tabs.TabHandler

public class DescriptionTab(tab: String, page: PageHandler): TabHandler(tab, page) {
	public fun buildDescription(ctx: RoutingContext): String {
		var extractor = ctx.data<ChannelExtractor>()["extractor"]
		if (extractor == null) {
			this.page.handle(ctx)
			extractor = ctx.data<ChannelExtractor>()["extractor"]
		}
		return extractor?.getDescription() ?: ""
	}

	public override fun handle(ctx: RoutingContext) {
		val description = this.buildDescription(ctx)
		ctx.data<String>().put("channelDescription", description)
	}
}
