// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.handlers

import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.*
import java.io.FileInputStream
import java.time.Instant
import kotlinx.coroutines.*
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.feed.FeedExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.local.subscription.workers.SubscriptionItem
import me.ganorton.youpipe.PageHandler
import me.ganorton.youpipe.managers.SettingsManager
import me.ganorton.youpipe.managers.SubscriptionManager
import me.ganorton.youpipe.utilities.FileUtility

public class SubscriptionHandler(basePath: String, subscriptionsPath: String) : PageHandler(basePath) {
	public override val supportHandlers: Map<String, (RoutingContext) -> Unit> = mapOf(
		"import" to ::handleImport,
		"all" to ::handleAllSubscriptions)

	public override fun handle(ctx: RoutingContext) {
		ctx.data<List<SubscriptionItem>>().put("subscriptions", SubscriptionManager.data)
		ctx.data<List<SubscriptionManager.ImportStrategy>>().put("importStrategies", SubscriptionManager.ImportStrategy.entries)
		ctx.data<List<SubscriptionManager.ExportSource>>().put("exportSources", SubscriptionManager.ExportSource.entries)
	}

	public fun handleImport(ctx: RoutingContext) {
		val importStrategy = ctx.request().getParam("importStrategy")
		val exportSource = ctx.request().getParam("exportSource")
		val importedContents = ctx.fileUploads()
			.flatMap { file ->
				val subs = FileInputStream(file.uploadedFileName()).use { SubscriptionManager.importStream(it, SubscriptionManager.ExportSource.valueOf(exportSource)) }
				file.delete()
				subs
			}

		SubscriptionManager.updateData(SubscriptionManager.ImportStrategy.valueOf(importStrategy), importedContents)
		ctx.redirect(this.basePath)
	}

	public fun handleAllSubscriptions(ctx: RoutingContext) {
		ctx.data<String>().put("pageTemplate", "subscriptions/all")

		println("SubscriptionHandler::handleAllSubscriptions (THIS WILL TAKE A WHILE)")

		val service = YoutubeService(0)
		val linkHandlerFactory = service.getChannelTabLHFactory()

		val failures = mutableListOf<SubscriptionItem>()
		/* 2 week cutoff, kludge until performance is improved */
		val cutoffInstant = Instant.ofEpochSecond(Instant.now().getEpochSecond() - (2 * 7 * 24 * 60 * 60))
		val fastFetching = SettingsManager.data.fastFetching

		val items = runBlocking {
			SubscriptionManager.data
				.map {
					async(ctx.vertx().dispatcher()) {
						val extractor =
							if (fastFetching)
								service.getFeedExtractor(it.url)
							else {
								var channelId = YoutubeChannelLinkHandlerFactory.getInstance().getId(it.url)
								val linkHandler = service.getChannelTabLHFactory().fromQuery(channelId, listOf(ChannelTabs.VIDEOS), "")
								service.getChannelTabExtractor(linkHandler)
							}
						val page = try {
							extractor.fetchPage()
							extractor.getInitialPage()
						} catch (e: Exception) {
							println(e)
							failures.add(it)
							null
						}
						//page?.getItems()?.filter { it.getUploadDate()?.getInstant()!!.isAfter(cutoffInstant) } ?: listOf<StreamInfoItem>()
						page?.getItems() ?: listOf<InfoItem>()
					}
				}
				.awaitAll()
				.filter { it != null }
				.flatMap { it }
				//.flatMap { it!!.getItems() }
				//.filter { it.getUploadDate()?.getInstant()!!.isAfter(cutoffInstant) }
				.sortedByDescending { (it as? StreamInfoItem)?.getUploadDate()?.getInstant()!!.getEpochSecond() ?: 0 }

		}
		//val items = listOf<StreamInfoItem>()
		/*val items = SubscriptionManager.data
			.map {
				val extractor = service.getFeedExtractor(it.url)
				try {
					extractor.fetchPage()
					extractor.getInitialPage()
				} catch (e: Exception) {
					failures.add(it)
					null
				}
			}
			.filter { it != null }
			.flatMap { it!!.getItems() }
			.filter { it.getUploadDate()?.getInstant()!!.isAfter(cutoffInstant) }
			.sortedByDescending { it.getUploadDate()?.getInstant()!!.getEpochSecond() ?: 0 }*/

		println("RESULT COUNT = ${items.size}, FAILURES = $failures")
		ctx.data<List<SubscriptionItem>>().put("failures", failures)
		ctx.data<List<InfoItem>>().put("listItems", items)
	}

	public fun handleAdd(ctx: RoutingContext) {
		println("SubscriptionHandler::handleAdd")
	}

	public fun handleRemove(ctx: RoutingContext) {
		println("SubscriptionHandler::handleRemove")
	}
}

