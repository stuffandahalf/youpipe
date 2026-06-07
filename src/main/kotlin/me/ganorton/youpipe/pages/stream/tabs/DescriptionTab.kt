// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages.stream.tabs

import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.stream.StreamExtractor
import me.ganorton.youpipe.pages.PageHandler
import me.ganorton.youpipe.pages.tabs.TabHandler

public class DescriptionTab(tabName: String, page: PageHandler) : TabHandler(tabName, page) {
	public override fun handle(ctx: RoutingContext) {
		val extractor = ctx.data<StreamExtractor>()["extractor"]
		ctx.data<String>().put("description", extractor?.getDescription()?.getContent() ?: "")
		ctx.data<String?>().put("externalUrl", extractor?.getUrl())
	}
}
