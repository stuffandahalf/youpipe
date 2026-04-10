// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe

public abstract class DataPageHandler<T>(protected val configFile: String, basePath: String, templateBase: String? = null) : PageHandler(basePath, templateBase) {
	private var lastLoaded: Int = 0
	protected var data: T? = null

	public fun loadIfNeeded() { }
}
