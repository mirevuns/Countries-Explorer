package com.countriesexplorer.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CachePolicyTest {

    @Test
    fun isStale_whenNeverSynced() {
        assertTrue(CachePolicy.isStale(null, 24))
    }

    @Test
    fun isStale_whenExpired() {
        val now = 1_000_000L
        val lastSync = now - (25L * 60L * 60L * 1000L)
        assertTrue(CachePolicy.isStale(lastSync, 24, now))
    }

    @Test
    fun isFresh_whenInsideTtl() {
        val now = 1_000_000L
        val lastSync = now - (1L * 60L * 60L * 1000L)
        assertFalse(CachePolicy.isStale(lastSync, 24, now))
    }
}
