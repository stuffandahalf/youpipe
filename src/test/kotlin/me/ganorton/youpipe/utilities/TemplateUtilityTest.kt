// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.utilities

import java.util.Locale
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.schabi.newpipe.extractor.Image

class TemplateUtilityTest {

	companion object {
		private var originalLocale: Locale? = null

		@BeforeAll
		fun pinDefaultLocale() {
			/* truncateNumber/formatNumber format via the default locale; pin it for deterministic output */
			originalLocale = Locale.getDefault()
			Locale.setDefault(Locale.US)
		}

		@AfterAll
		fun restoreDefaultLocale() {
			originalLocale?.let { Locale.setDefault(it) }
		}
	}

	@Test
	fun icon_withoutLabel() {
		assertEquals(
			"<span class=\"material-symbols-outlined\" >play_arrow</span>",
			TemplateUtility.icon("play_arrow", null))
	}

	@Test
	fun icon_withLabel() {
		assertEquals(
			"<span class=\"material-symbols-outlined\" aria-label=\"Settings\">settings</span>",
			TemplateUtility.icon("settings", "Settings"))
	}

	@Test
	fun truncateNumber_atOrBelowOneThousand_hasSpaceSuffix() {
		/* characterization: values at or below 1000 get the " " suffix from suffices[0]
		 * (the loop only divides while fnum > 1000) */
		assertEquals("0 ", TemplateUtility.truncateNumber(0L))
		assertEquals("999 ", TemplateUtility.truncateNumber(999L))
		assertEquals("1000 ", TemplateUtility.truncateNumber(1000L))
	}

	@ParameterizedTest
	@CsvSource(
		"9999, 10.0K",
		"12345, 12K",
		"999999, 1000K",
		"1234567, 1.2M",
		"1000000000, 1000M",
		"1500000000, 1.5B",
		"1000000000000, 1000B")
	fun truncateNumber(num: Long, expected: String) {
		assertEquals(expected, TemplateUtility.truncateNumber(num))
	}

	@Test
	fun truncateNumber_overOneTrillion_throws() {
		/* characterization: the suffices lookup overruns " KMB" for values above 1e12 (latent bug) */
		assertThrows<StringIndexOutOfBoundsException> {
			TemplateUtility.truncateNumber(2_000_000_000_000L)
		}
	}

	@ParameterizedTest
	@CsvSource(
		"0, 0",
		"999, 999")
	fun formatNumber(num: Long, expected: String) {
		assertEquals(expected, TemplateUtility.formatNumber(num))
	}

	@Test
	fun formatNumber_withGrouping() {
		/* kept out of @CsvSource because the grouped values contain the CSV delimiter */
		assertEquals("1,234", TemplateUtility.formatNumber(1234L))
		assertEquals("1,234,567", TemplateUtility.formatNumber(1234567L))
	}

	@Test
	fun buildImageSrcset_empty() {
		assertEquals("", TemplateUtility.buildImageSrcset(emptyList()))
	}

	@Test
	fun buildImageSrcset_single() {
		/* note: NewPipe's Image constructor is (url, height, width, level) */
		val imgs = listOf(
			Image("https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg", 94, 168, Image.ResolutionLevel.LOW))
		assertEquals(
			"https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg 168w",
			TemplateUtility.buildImageSrcset(imgs))
	}

	@Test
	fun buildImageSrcset_multiple() {
		val imgs = listOf(
			Image("https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg", 94, 168, Image.ResolutionLevel.LOW),
			Image("https://i.ytimg.com/vi/dQw4w9WgXcQ/maxres.jpg", 188, 336, Image.ResolutionLevel.MEDIUM))
		assertEquals(
			"https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg 168w, https://i.ytimg.com/vi/dQw4w9WgXcQ/maxres.jpg 336w",
			TemplateUtility.buildImageSrcset(imgs))
	}
}
