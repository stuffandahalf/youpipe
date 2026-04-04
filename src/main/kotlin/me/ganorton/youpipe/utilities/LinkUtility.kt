package me.ganorton.youpipe.utilities

import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeChannelLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeStreamLinkHandlerFactory

public object LinkUtility {
	public fun getChannelId(url: String): String {
		val id = YoutubeChannelLinkHandlerFactory.getInstance().getId(url)
		return id.substring(id.lastIndexOf('/'))
	}
	
	public fun getStreamId(url: String): String =
		YoutubeStreamLinkHandlerFactory.getInstance().getId(url)

	public fun getPlaylistId(url: String): String =
		YoutubePlaylistLinkHandlerFactory.getInstance().getId(url)
		
}
