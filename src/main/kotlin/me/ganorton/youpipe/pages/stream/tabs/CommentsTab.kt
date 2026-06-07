// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages.stream.tabs

import io.vertx.ext.web.RoutingContext
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.stream.StreamExtractor
import me.ganorton.youpipe.pages.PageHandler
import me.ganorton.youpipe.pages.tabs.TabHandler

public class CommentsTab(tabName: String, page: PageHandler) : TabHandler(tabName, page) {
		public override fun handle(ctx: RoutingContext) {
				val id = ctx.pathParam("id")
				println("VIDEO ID (COMMENTS) $id")

				val linkHandler = this.service.getCommentsLHFactory().fromId(id)
				val extractor = this.service.getCommentsExtractor(linkHandler)
				val disabled = extractor.isCommentsDisabled()
				ctx.data<Boolean>().put("commentsDisabled", disabled)
				if (disabled) {
					return
				}
				this.paginationHandler(ctx) { ctx -> extractor as ListExtractor<InfoItem>}
		}
}

