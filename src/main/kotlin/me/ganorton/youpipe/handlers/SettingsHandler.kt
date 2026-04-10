// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.handlers

import io.vertx.ext.web.RoutingContext
import me.ganorton.youpipe.PageHandler

public class SettingsHandler(basePath: String, private val configPath: String) : PageHandler(basePath) {
	private var lastLoaded: Int = 0
	private val settings: ApplicationSettings? = null

	public override fun handle(ctx: RoutingContext) {
		println("SettingsHandler::handle")
	}
}

//@Serializable
public data class ApplicationSettings(val tmp: String?)
