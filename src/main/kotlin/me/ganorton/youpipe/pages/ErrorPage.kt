// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages

import io.vertx.ext.web.RoutingContext
import me.ganorton.youpipe.BasePage

public class ErrorPage(basePath: String) : BasePage(basePath) {
	public override fun handle(ctx: RoutingContext) {
		if (ctx.data<Exception>()["exception"] == null) {
			ctx.redirect("/")
		}
	}
}

