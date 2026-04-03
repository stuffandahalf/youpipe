// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.handlers

import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import me.ganorton.youpipe.PageHandler

public class ChannelHandler(basePath: String) : PageHandler("$basePath/:channelId", basePath) {
	public override val tabHandlers: Map<String, PageHandler.Tab> = mapOf(
		"videos" to PageHandler.Tab(::handleVideoList),
		"shorts" to PageHandler.Tab(::handleShortsList),
		"live" to PageHandler.Tab(::handleLiveStreams),
		"playlists" to PageHandler.Tab(::handlePlaylists),
		"description" to PageHandler.Tab(::handleChannelDescription))

	protected override fun setup(ctx: RoutingContext) {
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

	public override fun handle(ctx: RoutingContext) {
		val channelId = ctx.pathParam("channelId")
		println("CHANNEL ID %s".format(channelId))
	}

	public fun handleVideoList(ctx: RoutingContext) {
	}
	
	public fun handleShortsList(ctx: RoutingContext) {
	}

	public fun handleLiveStreams(ctx: RoutingContext) {
	}

	public fun handlePlaylists(ctx: RoutingContext) {
	}

	public fun handleChannelDescription(ctx: RoutingContext) {
	}
}

