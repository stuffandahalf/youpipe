package me.ganorton.youpipe

import io.vertx.core.Future
import io.vertx.core.VerticleBase

class MainVerticle : VerticleBase() {

  override fun start() : Future<*> {
    return vertx
      .createHttpServer()
      .requestHandler { req ->
        req.response()
          .putHeader("content-type", "text/plain")
          .end("Hello from Vert.x!")
      }
    .listen(8888).onSuccess { http ->
      println("HTTP server started on port 8888")
    }
  }
}
