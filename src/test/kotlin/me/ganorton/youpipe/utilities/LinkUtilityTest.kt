// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.utilities

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.schabi.newpipe.extractor.exceptions.ParsingException

class LinkUtilityTest {

	@ParameterizedTest
	@CsvSource(
		"https://www.youtube.com/watch?v=dQw4w9WgXcQ, dQw4w9WgXcQ",
		"https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=42s&ab_channel=Rick_Astley, dQw4w9WgXcQ",
		"https://m.youtube.com/watch?v=dQw4w9WgXcQ, dQw4w9WgXcQ",
		"https://youtu.be/dQw4w9WgXcQ, dQw4w9WgXcQ",
		"https://youtu.be/dQw4w9WgXcQ?si=abc123def456, dQw4w9WgXcQ",
		"https://www.youtube.com/shorts/dQw4w9WgXcQ, dQw4w9WgXcQ",
		"https://www.youtube.com/embed/dQw4w9WgXcQ, dQw4w9WgXcQ",
		"vnd.youtube://dQw4w9WgXcQ, dQw4w9WgXcQ")
	fun getStreamId(url: String, expected: String) {
		assertEquals(expected, LinkUtility.getStreamId(url))
	}

	@ParameterizedTest
	@CsvSource(
		"https://www.youtube.com/watch?v=dQw4w9WgXcQ, /watch/dQw4w9WgXcQ",
		"https://youtu.be/dQw4w9WgXcQ, /watch/dQw4w9WgXcQ")
	fun buildStreamUrl(url: String, expected: String) {
		assertEquals(expected, LinkUtility.buildStreamUrl(url))
	}

	@ParameterizedTest
	@CsvSource(
		"https://www.youtube.com/playlist?list=PLabc123DEF456ghi789, PLabc123DEF456ghi789",
		"https://www.youtube.com/playlist?list=PLabc123DEF456ghi789&index=3, PLabc123DEF456ghi789",
		"https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=PLabc123DEF456ghi789, PLabc123DEF456ghi789")
	fun getPlaylistId(url: String, expected: String) {
		assertEquals(expected, LinkUtility.getPlaylistId(url))
	}

	@Test
	fun buildPlaylistUrl() {
		assertEquals(
			"/playlist/PLabc123DEF456ghi789",
			LinkUtility.buildPlaylistUrl("https://www.youtube.com/playlist?list=PLabc123DEF456ghi789"))
	}

	@ParameterizedTest
	@CsvSource(
		"https://www.youtube.com/channel/UCBrf3-zN4M5_If2R7obB-eA, UCBrf3-zN4M5_If2R7obB-eA",
		"https://m.youtube.com/channel/UCBrf3-zN4M5_If2R7obB-eA, UCBrf3-zN4M5_If2R7obB-eA",
		"https://www.youtube.com/@LinusTechTips, @LinusTechTips")
	fun getChannelId(url: String, expected: String) {
		assertEquals(expected, LinkUtility.getChannelId(url))
	}

	@ParameterizedTest
	@CsvSource(
		"https://www.youtube.com/channel/UCBrf3-zN4M5_If2R7obB-eA, /channel/UCBrf3-zN4M5_If2R7obB-eA",
		"https://www.youtube.com/@LinusTechTips, /channel/@LinusTechTips")
	fun buildChannelUrl(url: String, expected: String) {
		assertEquals(expected, LinkUtility.buildChannelUrl(url))
	}

	@Test
	fun getStreamId_playlistUrl_throws() {
		assertThrows<ParsingException> {
			LinkUtility.getStreamId("https://www.youtube.com/playlist?list=PLabc123DEF456ghi789")
		}
	}

	@Test
	fun getStreamId_nonYoutubeUrl_throws() {
		assertThrows<ParsingException> {
			LinkUtility.getStreamId("https://example.com/watch?v=dQw4w9WgXcQ")
		}
	}

	@Test
	fun getPlaylistId_withoutListParam_throws() {
		assertThrows<ParsingException> {
			LinkUtility.getPlaylistId("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
		}
	}

	@Test
	fun getChannelId_videoUrl_throws() {
		assertThrows<ParsingException> {
			LinkUtility.getChannelId("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
		}
	}
}
