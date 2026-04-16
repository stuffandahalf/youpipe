package me.ganorton.youpipe.utilities

import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory

public object LinkUtility {
	public fun getStreamId(url: String): String =
		YoutubeStreamLinkHandlerFactory.getInstance().getId(url)

	public fun buildStreamUrl(url: String): String =
		"/watch/${getStreamId(url)}"

	public fun getPlaylistId(url: String): String =
		YoutubePlaylistLinkHandlerFactory.getInstance().getId(url)

	public fun buildPlaylistUrl(url: String): String =
		"/playlist/${getPlaylistId(url)}"

	public fun getChannelId(url: String): String {
		var channelId = YoutubeChannelLinkHandlerFactory.getInstance().getId(url)
		if (channelId.startsWith("channel/")) {
			channelId = channelId.replace("channel/", "")
		}
		return channelId
	}

	public fun buildChannelUrl(url: String): String =
		"/channel/${getChannelId(url)}"
}
