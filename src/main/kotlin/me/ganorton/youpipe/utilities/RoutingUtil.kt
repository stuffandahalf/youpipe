// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.utilities

import io.vertx.ext.web.RoutingContext

/* browser URL update requested by a handler (e.g. HX-Push-Url / HX-Redirect) */
public data class RouteChangeOptions(
	var route: String? = null,
	var updateMethod: String? = "HX-Push-Url")

public object RoutingUtil {
	/* redirect to an app route, prefixing it with the reverse-proxy base path when present */
	public fun redirect(ctx: RoutingContext, path: String) {
		ctx.redirect(buildUrl(ctx, path))
	}

	/* join an app route with the current reverse-proxy base path */
	public fun buildUrl(ctx: RoutingContext, path: String): String {
		val baseUrl = ctx.data<String>()["baseUrl"] ?: ""
		return LinkUtility.buildUrl(baseUrl, path)
	}

	/* apply the browser URL update (HX-Push-Url / HX-Redirect / HX-Replace-Url) requested via
	   RouteChangeOptions; falls back to no URL push for fragment requests that didn't ask for one */
	public fun updateUrl(ctx: RoutingContext) {
		val opts = ctx.data<RouteChangeOptions>()["urlUpdateOptions"]
		if (opts?.updateMethod != null) {
			val route = opts.route ?: buildUrl(ctx, ctx.request().uri())
			ctx.response().putHeader(opts.updateMethod, route)
		} else {
			ctx.response().putHeader("HX-Push-Url", "false")
		}
	}
}
