package me.ganorton.youpipe

import io.vertx.core.MultiMap
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClient
import io.vertx.core.http.HttpMethod
import io.vertx.core.http.RequestOptions

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response

public class DownloaderImpl(val client: HttpClient) : Downloader() {
	public override fun execute(request: Request): Response? {
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

		val req = this.client.request(opts).await()
		val res = req.send(payload).await()
		val body = res.body().await().toString()
		//println(body)
		return Response(
			res.statusCode(),
			res.statusMessage(),
			this.mmToMl(res.headers()),
			body,
			res.request().absoluteURI())
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
