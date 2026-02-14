package me.ganorton.youpipe

import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.http.HttpServer
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler

class MainVerticle : VerticleBase() {

  override fun start() : Future<*> {
	val server: HttpServer = vertx.createHttpServer()
	val router: Router = Router.router(vertx)

	router.route("/apis/*").handler({ req ->
		req.response()
			.putHeader("content-type", "text/plain")
			.end("Hello from Vert.x!")
	})
	router.route().handler(StaticHandler.create("static/"))

	return server
		.requestHandler(router)
		.listen(8888).onSuccess { http ->
		  println("HTTP server started on port 8888")
		}
  }
}
