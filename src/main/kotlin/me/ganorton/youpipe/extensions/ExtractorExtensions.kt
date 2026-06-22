package me.ganorton.youpipe.extensions

import org.schabi.newpipe.extractor.Extractor

suspend fun Extractor.coFetchPage() {
	(suspend { this.fetchPage() })()
}

