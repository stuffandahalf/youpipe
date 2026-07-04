// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages

import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelTabExtractor
import org.schabi.newpipe.local.subscription.workers.SubscriptionItem
import me.ganorton.youpipe.BasePage
import me.ganorton.youpipe.managers.SubscriptionManager

public class ChannelPage(basePath: String) : BasePage("$basePath/:channelId", basePath) {
	public override val defaultTab = "videos"
	/* TODO: implement filtering based on channel available tabs? */
	public override val tabs: Array<TabHandler> = arrayOf(
		TabHandler(ChannelTabs.VIDEOS, { this.handleChannelTab(it, ChannelTabs.VIDEOS) }),
		TabHandler(ChannelTabs.SHORTS, { this.handleChannelTab(it, ChannelTabs.SHORTS) }),
		//TabHandler(ChannelTabs.LIVE, { this.handleChannelTab(it, ChannelTabs.LIVE) }),
		TabHandler(ChannelTabs.PLAYLISTS, { this.handleChannelTab(it, ChannelTabs.PLAYLISTS) }),
		TabHandler("description", this::handleDescription))

	public override val supportHandlers = mapOf(
		"subscribe" to ::handleSubscribe,
		"unsubscribe" to ::handleUnsubscribe)

	/*public override val tabHandlers: Array<PageHandler.Tab> = arrayOf(
		PageHandler.Tab("Videos", "videos", ::handleVideoList),
		PageHandler.Tab("Shorts", "shorts", ::handleShortsList),
		PageHandler.Tab("Live", "live", ::handleLiveStreams),
		PageHandler.Tab("Playlists", "playlists", ::handlePlaylists),
		PageHandler.Tab("Description", "description", ::handleChannelDescription))*/

	private fun isSubscribed(id: String): Boolean {
		val url = service.getChannelLHFactory().fromId("channel/$id").getUrl()
		return SubscriptionManager.data.any { it.url == url }
	}

	protected override fun filterTab(ctx: RoutingContext, tab: TabHandler): Boolean {
		if (tab.tabName == "description") {
			return true
		}

		val extractor = ctx.data<ChannelExtractor>()["extractor"]
		val availableTabs = extractor?.getTabs()?.flatMap { it.getContentFilters() }
		println("AVAILABLE TABS = $availableTabs")
		if (availableTabs == null) {
			return true
		}
		return availableTabs.contains(tab.tabName)
	}

	public override fun setup(ctx: RoutingContext) {
		super.setup(ctx)
		var channelExtractor = ctx.data<ChannelExtractor>()["extractor"];
		if (channelExtractor != null) {
			return
		}
		val channelId = ctx.pathParam("channelId")

		val linkHandler = service.getChannelLHFactory().fromId(channelId)
		channelExtractor = service.getChannelExtractor(linkHandler)
		channelExtractor.fetchPage()

		ctx.data<ChannelExtractor>().put("extractor", channelExtractor)
	}

	public override fun handle(ctx: RoutingContext) {
		val channelId = ctx.pathParam("channelId")
		ctx.data<Boolean>().put("isSubscribed", this.isSubscribed(channelId))
	}

	public fun handleChannelTab(ctx: RoutingContext, tab: String) {
		val channelId = ctx.pathParam("channelId")
		this.paginationHandler(ctx) { ctx ->
			val linkHandler = this.service.getChannelTabLHFactory().fromQuery(channelId, listOf(tab), "")
			this.service.getChannelTabExtractor(linkHandler)
		}
	}

	public fun handleDescription(ctx: RoutingContext) {
		var extractor = ctx.data<ChannelExtractor>()["extractor"]
		if (extractor == null) {
			this.handle(ctx)
			extractor = ctx.data<ChannelExtractor>()["extractor"]
		}
		val description = extractor?.getDescription() ?: ""
		ctx.data<String>().put("channelDescription", description)
	}


	public fun handleLiveStreams(ctx: RoutingContext) {
	}

	public fun manageSubscription(ctx: RoutingContext, modifier: (item: SubscriptionItem) -> Unit) {
		val channelId = ctx.pathParam("channelId")
		val extractor = ctx.data<ChannelExtractor>()["extractor"]
		if (extractor == null) {
			return@manageSubscription
		}
		val item = SubscriptionItem(this.service.getServiceId(), extractor?.getUrl() ?: "", extractor?.getName() ?: "")

		modifier(item)

		if (ctx.data<Boolean>()["isFragment"] == true) {
			ctx.data<Boolean>().put("isSubscribed", this.isSubscribed(channelId))
			ctx.data<String>().put("pageTemplate", "channel/subscribe-btn")
		} else {
			ctx.redirect(ctx.data<String>()["basePath"])
		}
	}

	public fun handleSubscribe(ctx: RoutingContext) = this.manageSubscription(ctx, SubscriptionManager::subscribe)
	public fun handleUnsubscribe(ctx: RoutingContext) = this.manageSubscription(ctx, SubscriptionManager::unsubscribe)
}

