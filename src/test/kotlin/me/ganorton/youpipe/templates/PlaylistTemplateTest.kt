// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.templates

import java.util.Locale
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.ext.web.templ.mvel.MVELTemplateEngine
import io.vertx.junit5.VertxExtension
import me.ganorton.youpipe.PaginationContext
import me.ganorton.youpipe.utilities.LinkUtility
import me.ganorton.youpipe.utilities.TemplateUtility
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.schabi.newpipe.extractor.Image
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor
import org.schabi.newpipe.extractor.playlist.PlaylistInfoItem
import org.schabi.newpipe.extractor.services.youtube.YoutubeService
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubePlaylistLinkHandlerFactory
import org.schabi.newpipe.extractor.stream.Description
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType

@ExtendWith(VertxExtension::class)
class PlaylistTemplateTest {

	private companion object {
		val noOpDownloader = object : Downloader() {
			override fun execute(request: Request): Response = throw UnsupportedOperationException("no network access in tests")
		}
		init {
			NewPipe.init(noOpDownloader)
		}

		var originalLocale: Locale? = null

		@BeforeAll
		fun pinDefaultLocale() {
			/* truncateNumber formats via the default locale; pin it for deterministic output */
			originalLocale = Locale.getDefault()
			Locale.setDefault(Locale.US)
		}

		@AfterAll
		fun restoreDefaultLocale() {
			originalLocale?.let { Locale.setDefault(it) }
		}
	}

	private val streamItem = StreamInfoItem(0, "https://www.youtube.com/watch?v=dQw4w9WgXcQ", "Test Stream", StreamType.VIDEO_STREAM).apply {
		setUploaderName("Test Uploader")
		setViewCount(1234)
		setTextualUploadDate("2 days ago")
		setThumbnails(listOf(Image("https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg", 336, 189, Image.ResolutionLevel.UNKNOWN)))
	}

	private val playlistItem = PlaylistInfoItem(0, "https://www.youtube.com/playlist?list=PLtest123456789", "Nested Playlist").apply {
		setThumbnails(emptyList())
	}

	private fun playlistExtractor(
		name: String = "Test Playlist",
		uploaderUrl: String = "https://www.youtube.com/channel/UCBrf3-zN4M5_If2R7obB-eA",
		uploaderName: String = "Test Channel",
		streamCount: Long = 1234,
		description: String = "Test description") = TestPlaylistExtractor(name, uploaderUrl, uploaderName, streamCount, description)

	private fun renderTemplate(vertx: Vertx, variables: Map<String, Any?>): String {
		val engine = MVELTemplateEngine.create(vertx, ".templ")
		val context = variables.toMutableMap()
		context["templateLoader"] = TestTemplateLoader(engine, context)
		return engine.render(context, "templates/playlist").await().toString()
	}

	private fun playlistContext(
		extractor: PlaylistExtractor = playlistExtractor(),
		listItems: List<InfoItem> = listOf(streamItem, playlistItem),
		isFragment: Boolean = false,
		pageNum: Int = 0,
		paginationPath: String? = null): Map<String, Any?> {
		val context = linkedMapOf<String, Any?>(
			"isFragment" to isFragment,
			"pageNum" to pageNum,
			"query" to "",
			"extractor" to extractor,
			"templateUtility" to TemplateUtility,
			"linkUtility" to LinkUtility,
			"listItems" to listItems,
			"paginationPath" to paginationPath)
		if (paginationPath != null) {
			context["paginationContext"] = PaginationContext(paginationPath, extractor as ListExtractor<InfoItem>, null, pageNum, null)
		}
		return context
	}

	@Test
	fun rendersTitleNameUploaderCountAndDescription(vertx: Vertx) {
		val html = renderTemplate(vertx, playlistContext())

		assertTrue(html.contains("<title>YouPipe | Playlist | Test Playlist</title>"))
		assertTrue(html.contains("https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg"))
		assertTrue(html.contains("Test Playlist"))
		assertTrue(html.contains("<a href=\"/channel/UCBrf3-zN4M5_If2R7obB-eA\">Test Channel</a>"))
		assertTrue(html.contains("1.2K videos"))
		assertTrue(html.contains("Test description"))
	}

	@Test
	fun rendersStreamAndPlaylistItemLinks(vertx: Vertx) {
		val html = renderTemplate(vertx, playlistContext())

		assertTrue(html.contains("<a href=\"/watch/dQw4w9WgXcQ\">Test Stream</a>"))
		assertTrue(html.contains("1.2K views - 2 days ago"))
		assertTrue(html.contains("<a href=\"/playlist/PLtest123456789\">Nested Playlist</a>"))
	}

	@Test
	fun hidesUploaderRow_whenUploaderUrlIsEmpty(vertx: Vertx) {
		val html = renderTemplate(vertx, playlistContext(extractor = playlistExtractor(uploaderUrl = "")))

		assertFalse(html.contains("href=\"/channel"))
		assertTrue(html.contains("Test Playlist"))
	}

	@Test
	fun hidesStreamCount_whenUnknown(vertx: Vertx) {
		val html = renderTemplate(vertx, playlistContext(extractor = playlistExtractor(streamCount = ListExtractor.ITEM_COUNT_UNKNOWN)))

		assertFalse(html.contains("videos"))
	}

	@Test
	fun hidesDescription_whenEmpty(vertx: Vertx) {
		val html = renderTemplate(vertx, playlistContext(extractor = playlistExtractor(description = "")))

		assertFalse(html.contains("Test description"))
	}

	@Test
	fun omitsTitle_onFragmentPageAfterFirst(vertx: Vertx) {
		val html = renderTemplate(vertx, playlistContext(isFragment = true, pageNum = 1))

		assertFalse(html.contains("<title>"))
	}

	@Test
	fun rendersPaginationForm_whenPaginationPathSet(vertx: Vertx) {
		val html = renderTemplate(vertx, playlistContext(paginationPath = "/playlist/PLtest123456789"))

		assertTrue(html.contains("action=\"/playlist/PLtest123456789\""))
		assertTrue(html.contains("Next Page"))
	}

	@Test
	fun omitsPaginationForm_whenNoPaginationPath(vertx: Vertx) {
		val html = renderTemplate(vertx, playlistContext())

		assertFalse(html.contains("Next Page"))
	}
}

class TestPlaylistExtractor(
	private val name: String,
	private val uploaderUrl: String,
	private val uploaderName: String,
	private val streamCount: Long,
	private val description: String) : PlaylistExtractor(YoutubeService(0), YoutubePlaylistLinkHandlerFactory.getInstance().fromId("PLtest123456789")) {

	override fun onFetchPage(downloader: Downloader) = Unit

	override fun getName(): String = name

	override fun getInitialPage(): InfoItemsPage<StreamInfoItem> = throw UnsupportedOperationException()

	override fun getPage(page: Page): InfoItemsPage<StreamInfoItem> = throw UnsupportedOperationException()

	override fun getUploaderUrl(): String = uploaderUrl

	override fun getUploaderName(): String = uploaderName

	override fun getUploaderAvatars(): List<Image> = emptyList()

	override fun isUploaderVerified(): Boolean = false

	override fun getStreamCount(): Long = streamCount

	override fun getDescription(): Description = Description(description, Description.HTML)

	override fun getThumbnails(): List<Image> = listOf(Image("https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg", 336, 189, Image.ResolutionLevel.UNKNOWN))
}

class TestTemplateLoader(private val engine: MVELTemplateEngine, private val context: Map<String, Any?>) {
	fun load(path: String): Buffer = engine.render(context, "templates/$path").await()
}
