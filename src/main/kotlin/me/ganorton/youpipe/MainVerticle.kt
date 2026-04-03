package me.ganorton.youpipe

import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.TemplateHandler
import io.vertx.ext.web.sstore.SessionStore
import io.vertx.ext.web.templ.mvel.MVELTemplateEngine
import org.schabi.newpipe.extractor.NewPipe
import me.ganorton.youpipe.handlers.ChannelHandler
import me.ganorton.youpipe.handlers.SearchHandler
import me.ganorton.youpipe.handlers.SubscriptionHandler
import me.ganorton.youpipe.handlers.VideoHandler

class MainVerticle : VerticleBase() {
	override fun start() : Future<*> {
		val templateDir = "templates"
		val staticDir = "static"

		val server: HttpServer = vertx.createHttpServer()
		val client: HttpClient = vertx.createHttpClient()
		val router: Router = Router.router(vertx)

		val engine = MVELTemplateEngine.create(vertx)
		val templateHandler = TemplateHandler.create(engine, templateDir, "text/html")
		val staticHandler = StaticHandler.create(staticDir)

		//val sessionStore = SessionStore.create(vertx)
		//val sessionHandler = SessionHandler.create(sessionStore)

		NewPipe.init(DownloaderImpl(client))

		/* site entrypoint */
		router.route("/").handler { ctx -> ctx.next() }

		router.route("/*").handler { ctx -> 
			val query = ctx.queryParams()["query"] ?: ""
			ctx.data<String>().put("query", query)
			ctx.next()
		}

		/* handlers */
		val channelHandler = ChannelHandler("/channel").attachTo(router)
		val searchHandler = SearchHandler("/search").attachTo(router)
		val videoHandler = VideoHandler("/watch").attachTo(router)
		val subHandler = SubscriptionHandler("/subscriptions").attachTo(router)

		val endpoints = router.getRoutes().map { r -> r.getPath() }

		/* template handler */
		router.route("/*")
			.handler { ctx ->
				/* set browser url to current request url unless handler set otherwise */
				if (ctx.data<Boolean>().get("hxCancelPush") != false) {
					val pushUrl = ctx.data<String>().get("hxPushUrl") ?: (ctx.request().uri())
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

				val path = ctx.request().path()
				var pageTemplate = ctx.data<String>().get("pageTemplate")
				var tabTemplate = ctx.data<String>().get("tabTemplate")

				if (pageTemplate == null && tabTemplate != null) {
					pageTemplate = tabTemplate
					tabTemplate = null
				}

				if (!endpoints.contains(path) && pageTemplate == null) {
					ctx.next()
					return@handler
				}
				try {
					val rootTemplate = templateDir + "/index"
					val isFragment = ctx.request().getHeader("HX-Request") != null
					ctx.data<Boolean>().put("isFragment", isFragment)

					if (path != "/") {
						if (pageTemplate == null) {
							pageTemplate = path
						}

						if (isFragment) {
							pageTemplate = templateDir + pageTemplate
						} else {
							pageTemplate = pageTemplate + ".templ"
						}
					}
					if (tabTemplate != null) {
						if (!isFragment) {
							tabTemplate = templateDir + tabTemplate
						}
						tabTemplate = tabTemplate + ".templ"
					}
					ctx.data<String>().set("pageTemplate", pageTemplate)
					ctx.data<String>().set("tabTemplate", tabTemplate)


					println("TEMPLATES - %s - %s - %s".format(rootTemplate, pageTemplate, tabTemplate))

					var content = engine.render(ctx.data(), if (isFragment) pageTemplate else rootTemplate).await()
					ctx.end(content)
				} catch (err: Throwable) {
					System.err.println(err.toString())
					ctx.next()
				}
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

