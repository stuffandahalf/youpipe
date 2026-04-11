// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import me.ganorton.youpipe.utilities.FileUtility

public abstract class DataManager<T>(private val configFile: String) {
	private var lastLoaded: Int = 0
	public var data: T? = null
		protected set

	init {
		this.load(true)
	}

	protected abstract fun mkInitData(): T
	public fun read(path: String): T = this.read(File(path))
	public fun read(handle: File): T = FileInputStream(handle).use { this.read(it) }
	public abstract fun read(stream: InputStream): T
	public abstract fun write(stream: OutputStream, data: T)

	private fun initData() {
		println("INIT DATA ($configFile)")
		this.data = this.mkInitData()
		// save data to configFile
		this.store()
	}

	public fun load(force: Boolean = false) {
		val fileHandle = File(this.configFile)
		if (force || this.lastLoaded == 0 || this.lastLoaded < fileHandle.lastModified()) {
			println("LOAD HERE (${this.configFile})")
		}

		val contents = FileUtility.readFile(fileHandle)
		println("CONFIG ($configFile) = $contents")
		if (contents == null) {
			this.initData()
		}

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
		//val contents = this.serialize(this.data!!)
		//FileUtility.writeFile(this.configFile, contents)
	}
}
