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

		val sessionStore = SessionStore.create(vertx)
		val sessionHandler = SessionHandler.create(sessionStore)

		NewPipe.init(DownloaderImpl(client))

		/* set browser url to current request url */
		router.route("/*")
			.handler(sessionHandler)
			.handler { ctx ->
				ctx.response().putHeader("HX-Push-Url", ctx.request().uri())
				ctx.next()
			}

		/* endpoints */
		router.route("/search").handler(SearchHandler())

		val videoHandler = VideoHandler()
		router.route("/watch").handler(videoHandler)
		//router.route("/watch/:video/stream").handler(videoHandler::handleStream)
		val subHandler = SubscriptionHandler()
		router.route("/subscriptions").method(HttpMethod.POST).handler(subHandler)
		//router.route("/subscriptions/import").handler(subHandler::handleImport)
		val endpoints = router.getRoutes().map { r -> r.getPath() }

		/* template handler */
		router.route("/*").handler { ctx ->
			val path = ctx.request().path()
			if (!endpoints.contains(path)) {
				ctx.next()
				return@handler
			}
			try {
				val isReload = ctx.request().getHeader("HX-Request") == null
				ctx.data<Boolean>().put("isReload", isReload)
				if (isReload) {
					ctx.data<String>().put("childTemplate", if (path.equals("/")) null else path + ".templ")
					val content = engine.render(ctx.data(), templateDir + "/index").await()
					ctx.end(content)
				} else {
					templateHandler.handle(ctx)
				}
			} catch (err: Throwable) {
				System.err.println(err.toString())
				ctx.next()
			}
		}

		/* fallback to static content if all else failed */
		router.route("/*").handler(staticHandler)
		/*router.routeWithRegex("/(?!apis/)[^\\.]*?(?!\\.[a-zA-Z0-9]+)$").handler { ctx ->
			ctx.response().sendFile("static/index.html")
		}*/

		return server
			.requestHandler(router)
			.listen(8888)
			.onSuccess { http ->
				println("HTTP server started on port 8888")
			}
	}
}

