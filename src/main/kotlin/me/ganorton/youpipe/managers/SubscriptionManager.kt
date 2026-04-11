// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.managers

import java.io.InputStream
import java.io.OutputStream
import org.schabi.newpipe.local.subscription.workers.ImportExportJsonHelper
import org.schabi.newpipe.local.subscription.workers.SubscriptionItem
import me.ganorton.youpipe.DataManager

public object SubscriptionManager : DataManager<List<SubscriptionItem>>("config/subscriptions.json") {
	override fun mkInitData(): List<SubscriptionItem> {
		println("SubscriptionHandler::initData")
		//return Subscriptions("1.0.0", 0, ArrayList<Subscription>())
		return listOf<SubscriptionItem>()
	}

	public override fun read(stream: InputStream): List<SubscriptionItem> =
		ImportExportJsonHelper.readFrom(stream)

	public override fun write(stream: OutputStream, data: List<SubscriptionItem>) =
		ImportExportJsonHelper.writeTo(data, stream)
}

