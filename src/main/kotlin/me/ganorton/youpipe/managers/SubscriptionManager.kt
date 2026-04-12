// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.managers

import java.io.InputStream
import java.io.OutputStream
import org.schabi.newpipe.local.subscription.workers.ImportExportJsonHelper
import org.schabi.newpipe.local.subscription.workers.SubscriptionItem
import me.ganorton.youpipe.DataManager

public object SubscriptionManager : DataManager<List<SubscriptionItem>>("config/subscriptions.json", listOf()) {
	public override fun read(stream: InputStream): List<SubscriptionItem> =
		ImportExportJsonHelper.readFrom(stream)

	public override fun write(stream: OutputStream, data: List<SubscriptionItem>) =
		ImportExportJsonHelper.writeTo(data, stream)

	public override fun updateData(strategy: String, newData: List<SubscriptionItem>) {
		val mergedData = when (strategy) {
			"merge" -> {
				var unifiedData = (this.data + newData)
				unifiedData = unifiedData.filterIndexed { i, d ->
					unifiedData.indexOfFirst { d2 -> d2 == d } != i
				}
				unifiedData.sortedWith { a, b -> a.name.compareTo(b.name) }
			}
			"overwrite" -> newData
			//"intersection" -> 
			else -> {
				System.err.println("Unsupported update strategy \"$strategy\"")
				null
			}
		}
		println("NEW DATA = $mergedData")
		if (mergedData != null) {
			this.data = mergedData
		}
		this.store()
	}
}

