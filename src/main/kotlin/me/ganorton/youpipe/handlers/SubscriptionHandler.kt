package me.ganorton.youpipe.handlers

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

public class SubscriptionHandler : Handler<RoutingContext> {
  public override fun handle(ctx: RoutingContext) {
    println("SubscriptionHandler::handle")
    val form = ctx.request().formAttributes()
    println(form.names())
    //val subList = ctx.request().getFormAttribute("subscriptions")
    //println(subList)
    ctx.next()
  }

  public fun handleImport(ctx: RoutingContext) {
    println("SubscriptionHandler::handleImport")
    ctx.next()
  }
}
