package com.countriesexplorer.testdoubles

import com.countriesexplorer.data.local.CacheMetadataDao
import com.countriesexplorer.data.local.CacheMetadataEntity
import com.countriesexplorer.data.local.CachedCountryEntity
import com.countriesexplorer.data.local.CollectionCountryEntity
import com.countriesexplorer.data.local.CollectionDao
import com.countriesexplorer.data.local.CollectionEntity
import com.countriesexplorer.data.local.CountryCacheDao
import com.countriesexplorer.data.local.CountryNoteDao
import com.countriesexplorer.data.local.CountryNoteEntity
import com.countriesexplorer.data.local.JournalEntryDao
import com.countriesexplorer.data.local.JournalEntryEntity
import com.countriesexplorer.data.local.VisitHistoryDao
import com.countriesexplorer.data.local.VisitHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeJournalEntryDao : JournalEntryDao {
    private val items = mutableListOf<JournalEntryEntity>()
    private val flows = mutableMapOf<Long, MutableStateFlow<List<JournalEntryEntity>>>()

    override fun getByProfileFlow(profileId: Long): Flow<List<JournalEntryEntity>> {
        return flows.getOrPut(profileId) {
            MutableStateFlow(items.filter { it.profileId == profileId })
        }
    }

    override suspend fun insert(entity: JournalEntryEntity) {
        items.add(0, entity.copy(id = (items.maxOfOrNull { it.id } ?: 0L) + 1))
        flows.keys.forEach { id ->
            flows[id]?.value = items.filter { it.profileId == id }
        }
    }

    override suspend fun deleteByIdForProfile(id: Long, profileId: Long) {
        items.removeAll { it.id == id && it.profileId == profileId }
        flows[profileId]?.value = items.filter { it.profileId == profileId }
    }

    override suspend fun clearForProfile(profileId: Long) {
        items.removeAll { it.profileId == profileId }
        flows[profileId]?.value = emptyList()
    }
}

class FakeCollectionDao : CollectionDao {
    private val collections = mutableListOf<CollectionEntity>()
    private val countries = mutableListOf<CollectionCountryEntity>()
    private val collectionFlows = mutableMapOf<Long, MutableStateFlow<List<CollectionEntity>>>()
    private val countryFlows = mutableMapOf<Long, MutableStateFlow<List<CollectionCountryEntity>>>()

    override fun getCollectionsFlow(profileId: Long): Flow<List<CollectionEntity>> {
        return collectionFlows.getOrPut(profileId) {
            MutableStateFlow(collections.filter { it.profileId == profileId }.sortedBy { it.name })
        }
    }

    override suspend fun getCollectionById(id: Long): CollectionEntity? =
        collections.firstOrNull { it.id == id }

    override fun getCountriesFlow(collectionId: Long): Flow<List<CollectionCountryEntity>> {
        return countryFlows.getOrPut(collectionId) {
            MutableStateFlow(countries.filter { it.collectionId == collectionId }.sortedBy { it.countryName })
        }
    }

    override suspend fun insertCollection(entity: CollectionEntity): Long {
        val id = (collections.maxOfOrNull { it.id } ?: 0L) + 1
        collections.add(entity.copy(id = id))
        refreshCollectionFlows()
        return id
    }

    override suspend fun insertCountry(entity: CollectionCountryEntity) {
        countries.removeAll { it.collectionId == entity.collectionId && it.countryCode == entity.countryCode }
        countries.add(entity)
        countryFlows[entity.collectionId]?.value =
            countries.filter { it.collectionId == entity.collectionId }.sortedBy { it.countryName }
    }

    override suspend fun deleteCollection(id: Long) {
        collections.removeAll { it.id == id }
        refreshCollectionFlows()
    }

    override suspend fun removeCountry(collectionId: Long, countryCode: String) {
        countries.removeAll { it.collectionId == collectionId && it.countryCode == countryCode }
        countryFlows[collectionId]?.value =
            countries.filter { it.collectionId == collectionId }.sortedBy { it.countryName }
    }

    override suspend fun deleteCollectionWithCountries(id: Long) {
        deleteCountriesInCollection(id)
        deleteCollection(id)
    }

    override suspend fun deleteCountriesInCollection(collectionId: Long) {
        countries.removeAll { it.collectionId == collectionId }
        countryFlows[collectionId]?.value = emptyList()
    }

    override suspend fun deleteCountriesForProfile(profileId: Long) {
        val ids = collections.filter { it.profileId == profileId }.map { it.id }
        countries.removeAll { it.collectionId in ids }
        ids.forEach { countryFlows[it]?.value = emptyList() }
    }

    override suspend fun deleteCollectionsForProfile(profileId: Long) {
        collections.removeAll { it.profileId == profileId }
        refreshCollectionFlows()
    }

    private fun refreshCollectionFlows() {
        collectionFlows.keys.forEach { profileId ->
            collectionFlows[profileId]?.value =
                collections.filter { it.profileId == profileId }.sortedBy { it.name }
        }
    }
}

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
    private val flows = mutableMapOf<Long, MutableStateFlow<List<VisitHistoryEntity>>>()
    private var nextId = 1L

    override fun getRecentFlow(profileId: Long, limit: Int): Flow<List<VisitHistoryEntity>> {
        return flows.getOrPut(profileId) {
            MutableStateFlow(items.filter { it.profileId == profileId }.sortedByDescending { it.visitedAt })
        }.map { list -> list.take(limit) }
    }

    override suspend fun insert(entity: VisitHistoryEntity) {
        val stored = entity.copy(id = nextId++)
        items.add(0, stored)
        flows.keys.forEach { profileId ->
            flows[profileId]?.value =
                items.filter { it.profileId == profileId }.sortedByDescending { it.visitedAt }
        }
    }

    override suspend fun deleteByCountryCode(profileId: Long, countryCode: String) {
        items.removeAll { it.profileId == profileId && it.countryCode == countryCode }
        flows[profileId]?.value =
            items.filter { it.profileId == profileId }.sortedByDescending { it.visitedAt }
    }

    override suspend fun clearForProfile(profileId: Long) {
        items.removeAll { it.profileId == profileId }
        flows[profileId]?.value = emptyList()
    }
}

class FakeCountryNoteDao : CountryNoteDao {
    private val notes = mutableMapOf<Pair<Long, String>, CountryNoteEntity>()
    private val flows = mutableMapOf<Pair<Long, String>, MutableStateFlow<CountryNoteEntity?>>()

    override suspend fun getByCountryCode(profileId: Long, countryCode: String): CountryNoteEntity? =
        notes[profileId to countryCode]

    override fun observeByCountryCode(profileId: Long, countryCode: String): Flow<CountryNoteEntity?> {
        return flows.getOrPut(profileId to countryCode) {
            MutableStateFlow(notes[profileId to countryCode])
        }
    }

    override suspend fun upsert(entity: CountryNoteEntity) {
        notes[entity.profileId to entity.countryCode] = entity
        flows.getOrPut(entity.profileId to entity.countryCode) { MutableStateFlow(entity) }.value = entity
    }

    override suspend fun deleteByCountryCode(profileId: Long, countryCode: String) {
        notes.remove(profileId to countryCode)
        flows[profileId to countryCode]?.value = null
    }

    override suspend fun deleteForProfile(profileId: Long) {
        notes.keys.filter { it.first == profileId }.forEach { key ->
            notes.remove(key)
            flows[key]?.value = null
        }
    }
}
