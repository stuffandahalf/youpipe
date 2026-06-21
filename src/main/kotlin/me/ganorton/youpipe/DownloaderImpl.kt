// Copyright (C) 2026 Gregory Norton
// SPDX-License-Identifier: GPL-3.0-only

package me.ganorton.youpipe

import io.vertx.core.MultiMap
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpClientRequest
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.RequestOptions

import io.vertx.kotlin.coroutines.*

import kotlinx.coroutines.*

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response

public class DownloaderImpl(val vertx: Vertx, val client: HttpClient) : Downloader() {
	public override fun execute(request: Request): Response? {
		return runBlocking { coExecute(request) }
	}

	private suspend fun coExecute(request: Request): Response? {
		/*GlobalScope.async(vertx.dispatcher) {
			val req = this.client.request(opts).coAwait()
			val res = req.send(payload*/

		println("%s (%s) REQUEST TO %s".format(request.httpMethod(), HttpMethod(request.httpMethod()), request.url()))

		val opts = RequestOptions()
		opts
			.setMethod(HttpMethod(request.httpMethod()))
			.setAbsoluteURI(request.url())
		request.headers().forEach { header, values ->
			opts.putHeader(header, values)
		}
		val payload = Buffer.buffer()
		if (request.dataToSend() != null) {
			payload.appendBytes(request.dataToSend())
		}
		/*val out = runBlocking {
			Thread.sleep(1000)
			"test"
		}
		println("OUT = \"$out\"")*/

		//val _client = this.client
		//var req: HttpClientRequest
		//var res: HttpClientResponse
		//var body: String
		println("REQ START")
		val req = GlobalScope.async<HttpClientRequest>(vertx.dispatcher()) {
			client.request(opts).coAwait()
		}.await()
		println("REQ END $req")

		/*println("RES START")
		val res = GlobalScope.async<HttpClientResponse>(vertx.dispatcher()) {
			req.send(payload).coAwait()
		}
		println("RES END $res")*/

		/*println("BODY START")
		val body = GlobalScope.async<Buffer>(vertx.dispatcher()) {
			res.body().coAwait()
		}
		println("BODY END $body")*/

		//val req = runBlocking<HttpClientRequest> { _client.request(opts) }
		//val req = GlobalScope.awaitResult<HttpClientRequest>(vertx.dispatcher()) { h ->
			//_client.request(opts)
		//}
		//println("REQ DONE $req")
		//GlobalScope.awaitResult<(vertx.dispatcher()) {
		/*val ctx = GlobalScope.async(vertx.dispatcher()) {
			println("REQ")
			req = _client.request(opts).coAwait()
			println("RES")
			res = awaitResult<HttpClientResponse> { h -> req.send(payload).onComplete(h) }
			println("BODY")
			body = awaitResult<Buffer> { h -> res.body().onComplete(h) }.toString()
			println("DONE")
		}*/
		return null
		/*return Response(
			res.statusCode(),
			res.statusMessage(),
			this.mmToMl(res.headers()),
			body,
			res.request().absoluteURI())*/
	}

	// convert vert.x MultiMap to NewPipe implementation
	private fun mmToMl(mm: MultiMap): Map<String, List<String>> {
		val ml = HashMap<String, List<String>>()
		for (k in mm.names()) {
			ml.put(k, mm.getAll(k))
		}
		return ml
	}
}
