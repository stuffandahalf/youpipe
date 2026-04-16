// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.managers

import java.io.InputStream
import java.io.OutputStream
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.local.subscription.workers.ImportExportJsonHelper
import org.schabi.newpipe.local.subscription.workers.SubscriptionItem
import me.ganorton.youpipe.DataManager

public object SubscriptionManager : DataManager<List<SubscriptionItem>, SubscriptionManager.ImportStrategy>("config/subscriptions.json", listOf()) {
	public override fun read(stream: InputStream): List<SubscriptionItem> =
		ImportExportJsonHelper.readFrom(stream)

	public override fun write(stream: OutputStream, data: List<SubscriptionItem>) =
		ImportExportJsonHelper.writeTo(data, stream)

	public fun importStream(stream: InputStream, source: ExportSource): List<SubscriptionItem> {
		if (source == ExportSource.YOUTUBE) {
			return YoutubeService(0).getSubscriptionExtractor().fromInputStream(stream) as List<SubscriptionItem>
		}
		return this.read(stream)
	}

	public override fun updateData(strategy: ImportStrategy, newData: List<SubscriptionItem>) {
		val mergedData = when (strategy) {
			ImportStrategy.MERGE -> {
				var unifiedData = (this.data + newData)
				unifiedData = unifiedData.filterIndexed { i, d ->
					unifiedData.indexOfFirst { d2 -> d2 == d } != i
				}
				unifiedData.sortedWith { a, b -> a.name.compareTo(b.name) }
			}
			ImportStrategy.OVERWRITE -> newData
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

	public enum class ImportStrategy {
		MERGE,
		OVERWRITE
	}

	public enum class ExportSource {
		YOUTUBE,
		NEWPIPE,
		YOUPIPE
	}
}

