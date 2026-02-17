package me.ganorton.youpipe

import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.TemplateHandler
import io.vertx.ext.web.templ.mvel.MVELTemplateEngine
import org.schabi.newpipe.extractor.NewPipe
import me.ganorton.youpipe.handlers.SearchHandler

class MainVerticle : VerticleBase() {
	override fun start() : Future<*> {
		val server: HttpServer = vertx.createHttpServer()
		val client: HttpClient = vertx.createHttpClient()
		val router: Router = Router.router(vertx)

		val engine = MVELTemplateEngine.create(vertx)
		val templateHandler = TemplateHandler.create(engine)

		NewPipe.init(DownloaderImpl(client))

		router.route("/apis/search").handler(SearchHandler())

		router.route("/apis/*").handler(templateHandler)
		router.route("/*").handler(StaticHandler.create("static/"))
		router.routeWithRegex("/(?!apis/)[^\\.]*?(?!\\.[a-zA-Z0-9]+)$").handler { ctx ->
			ctx.response().sendFile("static/index.html")
		}

		return server
			.requestHandler(router)
			.listen(8888)
			.onSuccess { http ->
				println("HTTP server started on port 8888")
			}
	}
}
