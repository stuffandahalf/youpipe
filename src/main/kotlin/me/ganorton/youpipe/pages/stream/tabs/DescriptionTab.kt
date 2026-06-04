// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages.stream.tab

import io.vertx.ext.web.RoutingContext
import me.ganorton.youpipe.pages.PageHandler
import me.ganorton.youpipe.pages.tabs.TabHandler

public class DescriptionTab(tabName: String, page: PageHandler) : TabHandler(tabName, page) {
	public override fun handle(ctx: RoutingContext) {
	}
}
