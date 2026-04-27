// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.handlers

import io.vertx.ext.web.RoutingContext
import me.ganorton.youpipe.PageHandler
import me.ganorton.youpipe.managers.SettingsManager
import me.ganorton.youpipe.managers.SubscriptionManager

public class SettingsHandler(basePath: String, private val configPath: String) : PageHandler(basePath) {
	public override val supportHandlers: Map<String, (RoutingContext) -> Unit> = mapOf(
		"update" to ::handleUpdate)

	public override fun handle(ctx: RoutingContext) {
		println("SettingsHandler::handle")

		ctx.data<SettingsManager.ApplicationSettings>().put("settings", SettingsManager.data)

		ctx.data<List<SubscriptionManager.ImportStrategy>>().put("importStrategies", SubscriptionManager.ImportStrategy.entries)
		ctx.data<List<SubscriptionManager.ExportSource>>().put("exportSources", SubscriptionManager.ExportSource.entries)
	}

	public fun handleUpdate(ctx: RoutingContext) {
		val req = ctx.request()
		val newSettings = SettingsManager.ApplicationSettings(
			fastFetching = req.getFormAttribute("fastFetching") == "on")
		SettingsManager.updateData(SettingsManager.ImportStrategy.OVERWRITE, newSettings)

		ctx.redirect(this.basePath)
	}
}

