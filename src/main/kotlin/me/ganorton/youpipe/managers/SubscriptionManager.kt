// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.managers

import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.local.subscription.workers.ImportExportJsonHelper
import org.schabi.newpipe.local.subscription.workers.SubscriptionItem
import me.ganorton.youpipe.DataManager

public object SubscriptionManager : DataManager<List<SubscriptionItem>, SubscriptionManager.ImportStrategy>("config/subscriptions.json", listOf()) {
	public var feedLastUpdated: Instant? = null
		private set
	public var feedFailures: MutableList<SubscriptionItem> = mutableListOf<SubscriptionItem>()
	public var feed: List<StreamInfoItem> = listOf<StreamInfoItem>()
		private set

	/* 2 week cutoff, kludge until performance is improved */
	private val cutoffInstant = Instant.ofEpochSecond(Instant.now().getEpochSecond() - (2 * 7 * 24 * 60 * 60))
	private val service = YoutubeService(0)
	private val linkHandlerFactory = service.getChannelTabLHFactory()

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


	public fun retrieveFeed(): List<StreamInfoItem> {
		this.feedFailures.clear()
		val fastFetching = SettingsManager.data.fastFetching

		this.feed = SubscriptionManager.data
			.map {
				val extractor =
					if (fastFetching)
						this.service.getFeedExtractor(it.url)
					else {
						var channelId = YoutubeChannelLinkHandlerFactory.getInstance().getId(it.url)
						val linkHandler = service.getChannelTabLHFactory().fromQuery(channelId, listOf(ChannelTabs.VIDEOS), "")
						service.getChannelTabExtractor(linkHandler)
					}

				try {
					extractor.fetchPage()
					extractor.getInitialPage()
				} catch (e: Exception) {
					this.feedFailures.add(it)
					null
				}
			}
			.filter { it != null }
			.flatMap { it!!.getItems() }
			.map { it as? StreamInfoItem }
			.filter { it != null && it.getUploadDate()?.getInstant()!!.isAfter(cutoffInstant) }
			.map { it as StreamInfoItem }
			.sortedByDescending { it!!.getUploadDate()?.getInstant()!!.getEpochSecond() ?: 0 }

		this.feedLastUpdated = Instant.now()
		return this.feed
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

