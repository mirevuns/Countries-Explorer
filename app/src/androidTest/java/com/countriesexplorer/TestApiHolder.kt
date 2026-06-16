package com.countriesexplorer

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.util.concurrent.atomic.AtomicInteger

object TestApiHolder {

    val server = MockWebServer()

    @Volatile
    private var started = false

    private val alphaErrorsRemaining = AtomicInteger(0)

    fun armNextAlphaErrors(count: Int) {
        alphaErrorsRemaining.set(count)
    }

    fun resetAlphaErrorArm() {
        alphaErrorsRemaining.set(0)
    }

    fun ensureStarted() {
        synchronized(this) {
            if (!started) {
                server.dispatcher = object : Dispatcher() {
                    override fun dispatch(request: RecordedRequest): MockResponse {
                        val path = request.path ?: return notFound()
                        if (path.contains("/code") && alphaErrorsRemaining.get() > 0) {
                            alphaErrorsRemaining.decrementAndGet()
                            return MockResponse()
                                .setResponseCode(500)
                                .setBody("{}")
                                .addHeader("Content-Type", "application/json")
                        }
                        return when {
                            path.contains("/region/") -> ok(TestResponses.regionCountriesJsonArray())
                            path.contains("/name") -> ok(TestResponses.searchCountriesJsonArray())
                            path.contains("/code") -> ok(TestResponses.singleCountryJsonResponse())
                            else -> notFound()
                        }
                    }
                }
                server.start()
                started = true
            }
        }
    }

    private fun ok(body: String) = MockResponse()
        .setBody(body)
        .setResponseCode(200)
        .addHeader("Content-Type", "application/json")

    private fun notFound() = MockResponse()
        .setResponseCode(404)
        .setBody("[]")
        .addHeader("Content-Type", "application/json")
}
