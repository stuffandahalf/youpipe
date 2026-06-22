// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.utilities

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.TemplateHandler
import io.vertx.ext.web.templ.mvel.MVELTemplateEngine
import io.vertx.kotlin.coroutines.coAwait
import kotlinx.coroutines.runBlocking

public class TemplateLoaderFactory(private val vertx: Vertx, private val templateDir: String) {
	private val templateExt = ".templ"

	private val engine = MVELTemplateEngine.create(this.vertx, this.templateExt)
	private val templateHandler = TemplateHandler.create(engine, templateDir, "text/html")

	public fun create(ctx: RoutingContext): TemplateLoader = TemplateLoader(ctx)
	
	public inner class TemplateLoader internal constructor(private val ctx: RoutingContext) {
		public fun load(path: String): Buffer {
			return runBlocking<Buffer> { coLoad(path) }
		}

		public suspend fun coLoad(path: String): Buffer {
			println("LOADING TEMPLATE \"$templateDir/$path\"")
			return engine.render(ctx.data(), templateDir + "/" + path).coAwait()
		}
	}
}
