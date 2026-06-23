package com.countriesexplorer.data.repository

import com.countriesexplorer.data.local.CollectionCountryEntity
import com.countriesexplorer.data.local.CollectionDao
import com.countriesexplorer.data.local.CollectionEntity
import com.countriesexplorer.data.model.Country
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class CollectionRepository @Inject constructor(
    private val collectionDao: CollectionDao,
    private val profileRepository: ProfileRepository
) {
    val collections: Flow<List<CollectionEntity>> = profileRepository.activeProfileId
        .flatMapLatest { profileId -> collectionDao.getCollectionsFlow(profileId) }

    suspend fun getOwnedCollectionById(id: Long): CollectionEntity? {
        val collection = collectionDao.getCollectionById(id) ?: return null
        return collection.takeIf { it.profileId == profileRepository.currentProfileId() }
    }

    fun getCountriesInCollection(collectionId: Long): Flow<List<CollectionCountryEntity>> {
        return profileRepository.activeProfileId.flatMapLatest { profileId ->
            flow {
                val collection = collectionDao.getCollectionById(collectionId)
                if (collection?.profileId == profileId) {
                    collectionDao.getCountriesFlow(collectionId).collect { emit(it) }
                } else {
                    emit(emptyList())
                }
            }
        }
    }

    suspend fun createCollection(name: String): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Название коллекции не может быть пустым" }
        val profileId = profileRepository.currentProfileId()
        return collectionDao.insertCollection(
            CollectionEntity(
                profileId = profileId,
                name = trimmed,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun addCountryToCollection(collectionId: Long, country: Country) {
        requireOwnedCollection(collectionId)
        collectionDao.insertCountry(CollectionCountryEntity.fromCountry(collectionId, country))
    }

    suspend fun removeCountryFromCollection(collectionId: Long, countryCode: String) {
        requireOwnedCollection(collectionId)
        collectionDao.removeCountry(collectionId, countryCode)
    }

    suspend fun deleteCollection(id: Long) {
        requireOwnedCollection(id)
        collectionDao.deleteCollectionWithCountries(id)
    }

    private suspend fun requireOwnedCollection(collectionId: Long): CollectionEntity {
        return getOwnedCollectionById(collectionId)
            ?: throw IllegalStateException("Коллекция недоступна для текущего профиля")
    }
}
