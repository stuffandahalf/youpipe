package me.ganorton.youpipe.handlers

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

public class VideoHandler : Handler<RoutingContext> {
	public override fun handle(ctx: RoutingContext) {
		ctx.next()
	}
}
