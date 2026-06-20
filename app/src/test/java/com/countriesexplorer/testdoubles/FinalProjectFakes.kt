package com.countriesexplorer.testdoubles

import com.countriesexplorer.data.local.CacheMetadataDao
import com.countriesexplorer.data.local.CacheMetadataEntity
import com.countriesexplorer.data.local.CachedCountryEntity
import com.countriesexplorer.data.local.CountryCacheDao
import com.countriesexplorer.data.local.CountryNoteDao
import com.countriesexplorer.data.local.CountryNoteEntity
import com.countriesexplorer.data.local.VisitHistoryDao
import com.countriesexplorer.data.local.VisitHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCountryCacheDao : CountryCacheDao {
    private val items = mutableListOf<CachedCountryEntity>()

    override suspend fun getAll(): List<CachedCountryEntity> = items.toList()

    override suspend fun getByCode(code: String): CachedCountryEntity? =
        items.firstOrNull { it.code.equals(code, ignoreCase = true) }

    override suspend fun insertAll(entities: List<CachedCountryEntity>) {
        entities.forEach { entity ->
            items.removeAll { it.code == entity.code }
            items.add(entity)
        }
    }

    override suspend fun clearAll() {
        items.clear()
    }

    override suspend fun count(): Int = items.size
}

class FakeCacheMetadataDao : CacheMetadataDao {
    var metadata: CacheMetadataEntity? = null

    override suspend fun get(): CacheMetadataEntity? = metadata

    override suspend fun upsert(metadata: CacheMetadataEntity) {
        this.metadata = metadata
    }
}

class FakeVisitHistoryDao : VisitHistoryDao {
    private val items = mutableListOf<VisitHistoryEntity>()
    private val flow = MutableStateFlow<List<VisitHistoryEntity>>(emptyList())
    private var nextId = 1L

    override fun getRecentFlow(limit: Int): Flow<List<VisitHistoryEntity>> =
        flow.map { list -> list.take(limit) }

    override suspend fun insert(entity: VisitHistoryEntity) {
        val stored = entity.copy(id = nextId++)
        items.add(0, stored)
        flow.value = items.toList()
    }

    override suspend fun deleteByCountryCode(countryCode: String) {
        items.removeAll { it.countryCode == countryCode }
        flow.value = items.toList()
    }

    override suspend fun clearAll() {
        items.clear()
        flow.value = emptyList()
    }
}

class FakeCountryNoteDao : CountryNoteDao {
    private val notes = mutableMapOf<String, CountryNoteEntity>()
    private val flows = mutableMapOf<String, MutableStateFlow<CountryNoteEntity?>>()

    override suspend fun getByCountryCode(countryCode: String): CountryNoteEntity? =
        notes[countryCode]

    override fun observeByCountryCode(countryCode: String): Flow<CountryNoteEntity?> {
        return flows.getOrPut(countryCode) {
            MutableStateFlow(notes[countryCode])
        }
    }

    override suspend fun upsert(entity: CountryNoteEntity) {
        notes[entity.countryCode] = entity
        flows.getOrPut(entity.countryCode) { MutableStateFlow(entity) }.value = entity
    }

    override suspend fun deleteByCountryCode(countryCode: String) {
        notes.remove(countryCode)
        flows[countryCode]?.value = null
    }
}
