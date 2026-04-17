// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe.utilities

import java.io.File
import java.io.InputStream
import java.nio.file.Files

public object FileUtility {
	public fun readStream(stream: InputStream): String {
		val sb = StringBuilder()

		var c = stream.read()
		while (c >= 0) {
			sb.append(c)
			c = stream.read()
		}
		return sb.toString()
	}

	public fun readFile(path: String): String? = this.readFile(File(path))
	public fun readFile(fileHandle: File): String? {
		if (!fileHandle.exists()) {
			return null
		}

		/* continue to load */
		val contents = Files.readAllLines(fileHandle.toPath()).joinToString("\n")
		println("CONTENTS (${fileHandle.getPath()}) = $contents")
		return contents
	}

	public fun writeFile(path: String, contents: String) = this.writeFile(File(path), contents)
	public fun writeFile(fileHandle: File, contents: String) {
		fileHandle.getParentFile().mkdirs()
		fileHandle.createNewFile()
	}
}
