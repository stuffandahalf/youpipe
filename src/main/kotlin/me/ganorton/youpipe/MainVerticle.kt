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

	/*router.route().handler({ req ->
		req.response.putHeader("content-type", "text/html")
	});*/

	router.route("/apis/*").handler { req ->
		req.response()
			.putHeader("content-type", "text/html")
			.end("<p>Hello from Vert.x!</p>")
	}
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
