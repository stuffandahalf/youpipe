// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages.tabs

import io.vertx.ext.web.Router
import me.ganorton.youpipe.BaseHandler
import me.ganorton.youpipe.pages.PageHandler

public abstract class TabHandler(public val tabName: String, public val page: PageHandler) : BaseHandler(page.basePath, page.templateBase) {
	public override fun attachTo(router: Router): TabHandler {
		router.route("${page.basePath}/$tabName").handler { ctx ->
			this.setup(ctx)
			this.handle(ctx)
			ctx.data<String>().put("tabTemplate", "${this.templatePrefix}/${this.tabName}")
			ctx.next()
		}
		return this
	}
}
