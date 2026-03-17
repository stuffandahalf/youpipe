package me.ganorton.youpipe.handlers

import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.channel.ChannelExtractor
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import me.ganorton.youpipe.BaseHandler

public class ChannelHandler(basePath: String) : BaseHandler(basePath) {
	public override fun attachTo(router: Router): BaseHandler {
		val base = this.basePath + "/:channelId"

		router.route(base).handler(::handle)
		router.route(base + "/videos").handler(::handleVideoList)
		router.route(base + "/shorts").handler(::handleShortsList)
		router.route(base + "/live").handler(::handleLiveStreams)
		router.route(base + "/playlists").handler(::handlePlaylists)
		router.route(base + "/description").handler(::handleChannelDescription)

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

