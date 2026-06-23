package com.countriesexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_metadata")
data class CacheMetadataEntity(
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    val lastFullSyncAt: Long? = null,
    val lastSyncStatus: String = STATUS_UNKNOWN
) {
    companion object {
        const val SINGLETON_ID = 1
        const val STATUS_UNKNOWN = "unknown"
        const val STATUS_SUCCESS = "success"
        const val STATUS_FAILED = "failed"
    }
}
