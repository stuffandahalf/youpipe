// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.managers

import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.ganorton.youpipe.DataManager
import me.ganorton.youpipe.utilities.FileUtility

public object SettingsManager : DataManager<SettingsManager.ApplicationSettings, SettingsManager.ImportStrategy>("config/preferences.json", ApplicationSettings(true)) {
	public override fun read(stream: InputStream): ApplicationSettings {
		val contents = FileUtility.readStream(stream)
		return Json.decodeFromString<ApplicationSettings>(contents)
	}

	public override fun write(stream: OutputStream, data: ApplicationSettings) =
		stream.write(Json.encodeToString(data).toByteArray())

	public override fun updateData(strategy: ImportStrategy, newData: ApplicationSettings) {
		this.data = newData
		this.store()
	}

	public enum class ImportStrategy {
		OVERWRITE
	}

	@Serializable
	public data class ApplicationSettings(val fastFetching: Boolean)
}

