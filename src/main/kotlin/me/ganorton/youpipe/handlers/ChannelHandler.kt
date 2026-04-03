// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.handlers

import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import me.ganorton.youpipe.PageHandler

public class ChannelHandler(basePath: String) : PageHandler(basePath) {
	val videoBase = this.basePath + "/:channelId"
	
	public override val tabHandlers: Map<String, PageHandler.Tab> = mapOf(
		"videos" to PageHandler.Tab(::handleVideoList))


	public override fun attachTo(router: Router): PageHandler {
		router.route(this.videoBase).handler(::handle)
		router.route(this.videoBase + "/videos").handler(::handleVideoList)
		router.route(this.videoBase + "/shorts").handler(::handleShortsList)
		router.route(this.videoBase + "/live").handler(::handleLiveStreams)
		router.route(this.videoBase + "/playlists").handler(::handlePlaylists)
		router.route(this.videoBase + "/description").handler(::handleChannelDescription)

		return this
	}

	private fun getChannelExtractor(channelId: String): ChannelExtractor {
		val service = YoutubeService(0)
		val linkHandler = service.getChannelLHFactory().fromId(channelId)
		val channelExtractor = service.getChannelExtractor(linkHandler)
		channelExtractor.fetchPage()
		return channelExtractor
	}

	public override fun handle(ctx: RoutingContext) {
		ctx.data<String>().put("pageTemplate", "/channel")
		val channelId = ctx.pathParam("channelId")

		println("CHANNEL ID %s".format(channelId))
		val channelExtractor = this.getChannelExtractor(channelId)

		ctx.data<ChannelExtractor>().put("channelExtractor", channelExtractor)

		ctx.next()
	}

	public fun handleVideoList(ctx: RoutingContext) {
		//this.initTab(ctx, this.basePath + "/videos", 
		val template = this.basePath + "/videos"
		ctx.data<String>().put("tabTemplate", template)
		ctx.next()
	}
	
	public fun handleShortsList(ctx: RoutingContext) {
		ctx.next()
	}

	public fun handleLiveStreams(ctx: RoutingContext) {
		ctx.next()
	}

	public fun handlePlaylists(ctx: RoutingContext) {
		ctx.next()
	}

	public fun handleChannelDescription(ctx: RoutingContext) {
		ctx.next()
	}
}

