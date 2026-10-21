// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.utilities

import java.io.ByteArrayInputStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FileUtilityTest {

	@Test
	fun readStream_returnsExactContents() {
		val contents = "{\"fastFetching\":false}"
		val result = FileUtility.readStream(ByteArrayInputStream(contents.toByteArray()))
		assertEquals(contents, result)
	}

	@Test
	fun readStream_preservesNewlines() {
		val contents = "line1\nline2\n"
		val result = FileUtility.readStream(ByteArrayInputStream(contents.toByteArray()))
		assertEquals(contents, result)
	}

	@Test
	fun readStream_emptyStream() {
		assertEquals("", FileUtility.readStream(ByteArrayInputStream(ByteArray(0))))
	}
}
