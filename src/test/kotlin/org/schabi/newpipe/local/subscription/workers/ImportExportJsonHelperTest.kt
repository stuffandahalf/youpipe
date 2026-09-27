// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package org.schabi.newpipe.local.subscription.workers

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.schabi.newpipe.BuildConfig
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor.InvalidSourceException

class ImportExportJsonHelperTest {

	@Test
	fun writeTo_readFrom_roundTrip() {
		val items = listOf(
			SubscriptionItem(0, "https://www.youtube.com/channel/UCBrf3-zN4M5_If2R7obB-eA", "Linus Tech Tips"),
			SubscriptionItem(0, "https://www.youtube.com/@SomeHandle", "Channel With 'Quote'"))

		val out = ByteArrayOutputStream()
		ImportExportJsonHelper.writeTo(items, out)

		assertEquals(items, ImportExportJsonHelper.readFrom(ByteArrayInputStream(out.toByteArray())))
	}

	@Test
	fun readFrom_parsesNewPipeExportFixture() {
		val stream = javaClass.classLoader.getResourceAsStream("fixtures/subscriptions_export.json")
		assertNotNull(stream)

		val items = ImportExportJsonHelper.readFrom(stream)

		assertEquals(3, items.size)
		assertEquals(0, items[0].serviceId)
		assertEquals("https://www.youtube.com/channel/UCSEtSu3HdMZgcjA1TzGCcow", items[0].url)
		assertEquals("8 Bit Guy YTP Productions", items[0].name)
		assertEquals("@LinusTechTips", items[1].url.removePrefix("https://www.youtube.com/"))
	}

	@Test
	fun writeTo_includesAppVersion() {
		val out = ByteArrayOutputStream()
		ImportExportJsonHelper.writeTo(emptyList(), out)

		val root = Json.parseToJsonElement(out.toString()).jsonObject
		assertEquals(BuildConfig.VERSION_NAME, root["app_version"]?.jsonPrimitive?.content)
		assertEquals(0, root["app_version_int"]?.jsonPrimitive?.content?.toInt())
	}

	@Test
	fun readFrom_malformedJson_throwsInvalidSource() {
		assertThrows<InvalidSourceException> {
			ImportExportJsonHelper.readFrom(ByteArrayInputStream("not json".toByteArray()))
		}
	}

	@Test
	fun readFrom_nullStream_throwsInvalidSource() {
		assertThrows<InvalidSourceException> {
			ImportExportJsonHelper.readFrom(null)
		}
	}
}
