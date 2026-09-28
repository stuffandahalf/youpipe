// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.pages

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.junit5.VertxExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class PlaylistPageTest {

	@Test
	fun isSinglePage_withoutTabs() {
		val page = PlaylistPage("/playlist")

		assertNull(page.defaultTab)
		assertTrue(page.tabs.isEmpty())
	}

	@Test
	fun attachTo_registersOnlyPlaylistRoute(vertx: Vertx) {
		val router = Router.router(vertx)

		PlaylistPage("/playlist").attachTo(router)

		assertEquals(listOf("/playlist/:id"), router.getRoutes().map { it.getPath() })
	}
}
