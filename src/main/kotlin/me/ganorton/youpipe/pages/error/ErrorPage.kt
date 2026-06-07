// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages.error

import io.vertx.ext.web.RoutingContext
import me.ganorton.youpipe.pages.PageHandler

public class ErrorPage(basePath: String) : PageHandler(basePath) {
	public override fun handle(ctx: RoutingContext) {
		if (ctx.data<Exception>()["exception"] == null) {
			ctx.redirect("/")
		}
	}
}

