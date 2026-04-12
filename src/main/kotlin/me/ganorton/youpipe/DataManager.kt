// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import me.ganorton.youpipe.utilities.FileUtility

public abstract class DataManager<T>(private val configFile: String, public var data: T) {
	private var lastLoaded: Long = 0

	init {
		this.load(true)
	}

	public abstract fun updateData(strategy: String, newData: T)
	public fun read(path: String): T = this.read(File(path))
	public fun read(handle: File): T = FileInputStream(handle).use { this.read(it) }
	public abstract fun read(stream: InputStream): T
	public abstract fun write(stream: OutputStream, data: T)

	private fun initData() {
		this.store()
	}

	public fun load(force: Boolean = false) {
		val fileHandle = File(this.configFile)

		if (!fileHandle.isFile) {
			this.store()
			return
		}

		if (force || this.lastLoaded == 0L || this.lastLoaded < fileHandle.lastModified()) {
			println("LOAD HERE (${this.configFile})")
		}

		try {
			val newData = FileInputStream(fileHandle).use { this.read(it) }
			this.data = newData
			//this.lastLoaded = Instant.now().toEpochMilli()
			//println("TIMESTAMPS NOW = ${Instant.now().getEpochSecond()}, FILE = ${fileHandle.toEpochMilli()}")
		} catch (e: Exception) {
			System.err.println("Failed to load \"$configFile\": $e")
		}



		/*val contents = FileUtility.readFile(fileHandle)
		println("CONFIG ($configFile) = $contents")
		if (contents == null) {
			//this.initData()

		}*/

		/*if (fileHandle.isDirectory()) {
			throw 
		}*/

		/*val fileTimestamp = fileHandle.lastModified()
		if (!force || this.lastLoaded > fileTimestamp) {
			return
		}*/
		//this.data = Json.decodeFromString<T>(contents)
		/*FileReader(fileHandle).use { reader ->
			val contents = reader.
		}*/
	}

	public fun store() {
		val fileHandle = File(this.configFile)
		if (!fileHandle.isFile) {
			fileHandle.getParentFile().mkdirs()
			fileHandle.createNewFile()
		}
		FileOutputStream(fileHandle).use { this.write(it, this.data) }
	}
}
