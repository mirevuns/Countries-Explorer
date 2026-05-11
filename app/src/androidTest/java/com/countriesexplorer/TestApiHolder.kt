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
                        val path = request.path ?: ""
                        if (path.contains("/alpha/") && alphaErrorsRemaining.get() > 0) {
                            alphaErrorsRemaining.decrementAndGet()
                            return MockResponse()
                                .setResponseCode(500)
                                .setBody("{}")
                                .addHeader("Content-Type", "application/json")
                        }
                        return MockResponse()
                            .setBody(TestResponses.singleCountryJsonArray())
                            .setResponseCode(200)
                            .addHeader("Content-Type", "application/json")
                    }
                }
                server.start()
                started = true
            }
        }
    }
}
