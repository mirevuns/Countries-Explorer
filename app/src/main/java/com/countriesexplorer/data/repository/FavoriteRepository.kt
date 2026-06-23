package com.countriesexplorer.data.repository

import com.countriesexplorer.data.local.FavoriteDao
import com.countriesexplorer.data.local.FavoriteEntity
import com.countriesexplorer.data.model.Country
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class FavoriteRepository @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val profileRepository: ProfileRepository
) {
    val favoriteCodes: Flow<Set<String>> = profileRepository.activeProfileId
        .flatMapLatest { profileId ->
            favoriteDao.getAllFavoriteCodes(profileId).map { it.toSet() }
        }

    val favorites: Flow<List<FavoriteEntity>> = profileRepository.activeProfileId
        .flatMapLatest { profileId -> favoriteDao.getAllFavoritesFlow(profileId) }

    suspend fun toggleFavorite(country: Country) {
        val profileId = profileRepository.currentProfileId()
        favoriteDao.toggleFavorite(FavoriteEntity.fromCountry(country, profileId))
    }

    suspend fun removeFavorite(code: String) {
        favoriteDao.deleteByCode(profileRepository.currentProfileId(), code)
    }
}
