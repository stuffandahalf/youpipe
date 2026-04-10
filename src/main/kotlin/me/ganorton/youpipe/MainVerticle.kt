// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe

import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.sstore.SessionStore
import org.schabi.newpipe.extractor.NewPipe
import me.ganorton.youpipe.handlers.ChannelHandler
import me.ganorton.youpipe.handlers.PlaylistHandler
import me.ganorton.youpipe.handlers.SearchHandler
import me.ganorton.youpipe.handlers.SettingsHandler
import me.ganorton.youpipe.handlers.SubscriptionHandler
import me.ganorton.youpipe.handlers.VideoHandler
import me.ganorton.youpipe.utilities.LinkUtility
import me.ganorton.youpipe.utilities.TemplateLoaderFactory
import me.ganorton.youpipe.utilities.TemplateUtility

class MainVerticle : VerticleBase() {
	override fun start() : Future<*> {
		val mobileBreakpoint = "768px"
		val templateDir = "templates"
		val templateExt = ".templ"
		val staticDir = "static"

		val settingsFile = "settings.json"
		val subscriptionFile = "subscriptions.json"

		val server: HttpServer = vertx.createHttpServer()
		val client: HttpClient = vertx.createHttpClient()
		val router: Router = Router.router(vertx)

		val sessionStore = SessionStore.create(vertx)

		val templateLoaderFactory = TemplateLoaderFactory(vertx, templateDir)
		val sessionHandler = SessionHandler.create(sessionStore)
		val staticHandler = StaticHandler.create(staticDir)

		NewPipe.init(DownloaderImpl(client))

		/* site entrypoint */
		router.route("/").handler { ctx ->
			ctx.data<String>().put("pageTemplate", "home")
			ctx.next()
		}
		//router.route("/").handler { ctx -> ctx.redirect("/subscriptions") }

		router.route()
		.handler(sessionHandler)
		.handler { ctx ->
			/* CSS shenanigans */
			ctx.data<String>().put("mobileBreakpoint", mobileBreakpoint)
			ctx.data<String>().put("isMobile", "screen and (width < $mobileBreakpoint)")

			/* add template utilities */
			ctx.data<Boolean>().put("isFragment", ctx.request().getHeader("HX-Request") != null)
			ctx.data<String>().put("templateRoot", templateDir)
			ctx.data<TemplateUtility>().put("templateUtility", TemplateUtility)
			ctx.data<LinkUtility>().put("linkUtility", LinkUtility)

			/* repopulate search bar if refreshed */
			val query = ctx.queryParams()["query"] ?: ""
			ctx.data<String>().put("query", query)
			ctx.next()
		}

		/* handlers */
		val channelHandler = ChannelHandler("/channel").attachTo(router)
		val playlistHandler = PlaylistHandler("/playlists").attachTo(router)
		val searchHandler = SearchHandler("/search").attachTo(router)
		val settingsHandler = SettingsHandler("/settings", settingsFile).attachTo(router)
		val subscriptionHandler = SubscriptionHandler("/subscriptions", subscriptionFile).attachTo(router)
		val videoHandler = VideoHandler("/watch").attachTo(router)

		val endpoints = router.getRoutes().map { r -> r.getPath() }

		/* template handler */
		router.route("/*")
			.handler { ctx ->
				/* set browser url to current request url unless handler set otherwise */
				if (ctx.data<Boolean>()["hxCancelPush"] != false) {
					val pushUrl = ctx.data<String>()["hxPushUrl"] ?: (ctx.request().uri())
					ctx.response().putHeader("HX-Push-Url", pushUrl)
				}
				ctx.next()
			}
			.handler { ctx ->
				/* template handling */

				// hierarchy
				// - index.templ
				// - data.pageTemplate
				// - data.tabTemplates

				val templateLoader = templateLoaderFactory.create(ctx)
				ctx.data<TemplateLoaderFactory.TemplateLoader>().put("templateLoader", templateLoader)

				val isFragment = ctx.data<Boolean>()["isFragment"] ?: false
				val rootTemplate = "index"
				val pageTemplate = ctx.data<String>()["pageTemplate"]
				val tabTemplate = ctx.data<String>()["tabTemplate"]

				ctx.data<String>().put("pageTemplate", pageTemplate ?: tabTemplate ?: null)

				/* don't try to render static assets */
				val path = ctx.request().path()
				if (path != "/" && pageTemplate == null && tabTemplate == null) {
					println("SKIP RENDER ($path)")
					ctx.next()
					return@handler
				}

				println("TEMPLATES ($isFragment) - $rootTemplate - $pageTemplate - $tabTemplate")

				//try {
					val template = if (!isFragment) rootTemplate else (pageTemplate ?: tabTemplate)
					val content = templateLoader.load(template!!)
					ctx.end(content)
				/*} catch (err: Throwable) {
					System.err.println(err.toString())
					ctx.next()
				}*/
			}

		/* fallback to static content if all else failed */
		router.route("/*").handler(staticHandler)

		return server
			.requestHandler(router)
			.listen(8888)
			.onSuccess { http ->
				println("HTTP server started on port 8888")
			}
	}
}

