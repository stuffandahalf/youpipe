package me.ganorton.youpipe.utilities

import io.vertx.core.http.HttpServerRequest
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory

public object LinkUtility {
	/* header carrying the URL the reverse proxy received before any rewrite */
	private const val PROXY_URL_HEADER = "X-Forwarded-For"

	/* base path of the site for this request, derived from the difference between the URL the
	   proxy received and the path Vert.x was given ("" when the app is not proxied) */
	public fun baseUrl(request: HttpServerRequest): String =
		baseUrlOf(request.getHeader(PROXY_URL_HEADER), request.path())

	/* pure delta computation used by baseUrl(); kept separate so it can be unit tested */
	public fun baseUrlOf(proxyUrl: String?, receivedPath: String): String {
		if (proxyUrl == null) {
			return ""
		}

		val original = pathOf(proxyUrl)
		if (original.isEmpty() || original == receivedPath) {
			return ""
		}

		var base = ""
		if (receivedPath.isNotEmpty() && original.endsWith(receivedPath)) {
			base = original.substring(0, original.length - receivedPath.length)
		} else if (receivedPath == "/") {
			base = original
		}

		base = base.replace("//", "/").trimEnd('/')
		if (base.isEmpty() || !base.startsWith('/') || base.contains("..")) {
			return ""
		}
		return base
	}

	/* path portion of a proxied URL; accepts a full URL or a bare path, dropping query/fragment.
	   Values that are not paths (e.g. the client IP nginx normally puts in X-Forwarded-For) yield "". */
	private fun pathOf(url: String): String {
		var value = url.trim()

		val schemeEnd = value.indexOf("://")
		if (schemeEnd >= 0) {
			val firstSlash = value.indexOf('/', schemeEnd + 3)
			value = if (firstSlash < 0) "" else value.substring(firstSlash)
		}

		val queryStart = value.indexOf('?')
		if (queryStart >= 0) {
			value = value.substring(0, queryStart)
		}
		val fragmentStart = value.indexOf('#')
		if (fragmentStart >= 0) {
			value = value.substring(0, fragmentStart)
		}

		return if (value.startsWith('/')) value else ""
	}

	/* join a base path with an absolute route (no-op when there is no base path) */
	public fun buildUrl(baseUrl: String, path: String): String {
		if (baseUrl.isEmpty()) {
			return path
		}
		if (path.isEmpty() || path == "/") {
			return "$baseUrl/"
		}
		return baseUrl + (if (path.startsWith('/')) path else "/$path")
	}

	public fun getStreamId(url: String): String =
		YoutubeStreamLinkHandlerFactory.getInstance().getId(url)

	public fun buildStreamUrl(baseUrl: String, url: String): String =
		buildUrl(baseUrl, "/watch/${getStreamId(url)}")

	public fun getPlaylistId(url: String): String =
		YoutubePlaylistLinkHandlerFactory.getInstance().getId(url)

	public fun buildPlaylistUrl(baseUrl: String, url: String): String =
		buildUrl(baseUrl, "/playlist/${getPlaylistId(url)}")

	public fun getChannelId(url: String): String {
		var channelId = YoutubeChannelLinkHandlerFactory.getInstance().getId(url)
		if (channelId.startsWith("channel/")) {
			channelId = channelId.replace("channel/", "")
		}
		return channelId
	}

	public fun buildChannelUrl(baseUrl: String, url: String): String =
		buildUrl(baseUrl, "/channel/${getChannelId(url)}")
}
