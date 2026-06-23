package com.countriesexplorer.testdoubles

import com.countriesexplorer.data.local.FavoriteDao
import com.countriesexplorer.data.local.FavoriteEntity
import com.countriesexplorer.data.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeFavoriteDao : FavoriteDao {

    private val entities = MutableStateFlow<List<FavoriteEntity>>(emptyList())

    override fun getAllFavoritesFlow(profileId: Long): Flow<List<FavoriteEntity>> =
        entities.map { list -> list.filter { it.profileId == profileId } }

    override fun getAllFavoriteCodes(profileId: Long): Flow<List<String>> =
        getAllFavoritesFlow(profileId).map { list -> list.map { it.code } }

    override suspend fun insert(entity: FavoriteEntity) {
        entities.update { current ->
            (current.filter { it.profileId != entity.profileId || it.code != entity.code } + entity)
                .sortedBy { it.name }
        }
    }

    override suspend fun deleteByCode(profileId: Long, code: String) {
        entities.update { it.filter { e -> !(e.profileId == profileId && e.code == code) } }
    }

    override suspend fun isFavorite(profileId: Long, code: String): Boolean =
        entities.value.any { it.profileId == profileId && it.code == code }

    override suspend fun deleteForProfile(profileId: Long) {
        entities.update { it.filter { e -> e.profileId != profileId } }
    }

    override suspend fun toggleFavorite(entity: FavoriteEntity) {
        if (isFavorite(entity.profileId, entity.code)) {
            deleteByCode(entity.profileId, entity.code)
        } else {
            insert(entity)
        }
    }
}
