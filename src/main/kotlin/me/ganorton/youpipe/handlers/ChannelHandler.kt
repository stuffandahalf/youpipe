// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.handlers

import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.extractors.YoutubeChannelTabExtractor
import me.ganorton.youpipe.PageHandler

public class ChannelHandler(basePath: String) : PageHandler("$basePath/:channelId", basePath) {
	public override val defaultTab = "videos"
	/* TODO: implement filtering based on channel available tabs? */
	public override val tabHandlers: Array<PageHandler.Tab> = arrayOf(
		PageHandler.Tab("Videos", "videos", ::handleVideoList),
		PageHandler.Tab("Shorts", "shorts", ::handleShortsList),
		PageHandler.Tab("Live", "live", ::handleLiveStreams),
		PageHandler.Tab("Playlists", "playlists", ::handlePlaylists),
		PageHandler.Tab("Description", "description", ::handleChannelDescription))

	protected override fun filterTab(ctx: RoutingContext, tab: PageHandler.Tab): Boolean {
		if (tab.target == "description") {
			return true
		}

		val extractor = ctx.data<ChannelExtractor>()["extractor"]
		val availableTabs = extractor?.getTabs()?.flatMap { it.getContentFilters() }
		println("AVAILABLE TABS = $availableTabs")
		if (availableTabs == null) {
			return true
		}
		return availableTabs.contains(tab.target)
	}

	public override fun handle(ctx: RoutingContext) {
		var channelExtractor = ctx.data<ChannelExtractor>()["extractor"];
		if (channelExtractor != null) {
			return
		}
		val channelId = ctx.pathParam("channelId")

		val service = YoutubeService(0)
		val linkHandler = service.getChannelLHFactory().fromId(channelId)
		channelExtractor = service.getChannelExtractor(linkHandler)
		channelExtractor.fetchPage()

		ctx.data<ChannelExtractor>().put("extractor", channelExtractor)
	}

	private fun handleChannelTab(ctx: RoutingContext, tab: String) {
		val channelId = ctx.pathParam("channelId")

		val service = YoutubeService(0)
		val linkHandler = service.getChannelTabLHFactory().fromQuery(channelId, listOf(tab), "")
		val channelTabExtractor = service.getChannelTabExtractor(linkHandler)
		channelTabExtractor.fetchPage()

		val page = channelTabExtractor.getInitialPage()
		ctx.data<List<InfoItem>>().put("listItems", page.getItems())
	}

	/* TODO: implement video list paging */
	public fun handleVideoList(ctx: RoutingContext) {
		this.handleChannelTab(ctx, ChannelTabs.VIDEOS)
	}
	
	public fun handleShortsList(ctx: RoutingContext) {
		this.handleChannelTab(ctx, ChannelTabs.SHORTS)
	}

	public fun handleLiveStreams(ctx: RoutingContext) {
	}

	public fun handlePlaylists(ctx: RoutingContext) {
		this.handleChannelTab(ctx, ChannelTabs.PLAYLISTS)
	}

	public fun handleChannelDescription(ctx: RoutingContext) {
		this.handle(ctx)

		val channelExtractor = ctx.data<ChannelExtractor>()["extractor"]
		ctx.data<String>().put("channelDescription", channelExtractor?.getDescription() ?: "")
	}
}

