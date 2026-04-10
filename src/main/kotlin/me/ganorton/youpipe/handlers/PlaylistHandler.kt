// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.handlers

import io.vertx.ext.web.RoutingContext
import me.ganorton.youpipe.PageHandler

public class PlaylistHandler(basePath: String) : PageHandler(basePath) {
	public override fun handle(ctx: RoutingContext) {
		println("PlaylistHandler::handle")
	}
}
