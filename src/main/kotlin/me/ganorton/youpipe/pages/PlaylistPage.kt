// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages

import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import me.ganorton.youpipe.BasePage

public class PlaylistPage(basePath: String) : BasePage("$basePath/:id", basePath) {
	protected override fun setup(ctx: RoutingContext) {
		super.setup(ctx)

		var playlistExtractor = ctx.data<PlaylistExtractor>()["extractor"]
		if (playlistExtractor != null) {
			return
		}

		val playlistId = ctx.pathParam("id")
		val linkHandler = this.service.getPlaylistLHFactory().fromId(playlistId)
		playlistExtractor = this.service.getPlaylistExtractor(linkHandler)
		playlistExtractor.fetchPage()

		ctx.data<PlaylistExtractor>().put("extractor", playlistExtractor)
	}

	public override fun handle(ctx: RoutingContext) {
		val extractor = ctx.data<PlaylistExtractor>()["extractor"]
		this.paginationHandler(ctx) { _ ->
			extractor as ListExtractor<InfoItem>
		}
	}
}
