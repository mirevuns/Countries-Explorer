package com.countriesexplorer.testdoubles

import com.countriesexplorer.data.local.FavoriteDao
import com.countriesexplorer.data.local.FavoriteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeFavoriteDao : FavoriteDao {

    private val entities = MutableStateFlow<List<FavoriteEntity>>(emptyList())

    override fun getAllFavoritesFlow(): Flow<List<FavoriteEntity>> = entities

    override fun getAllFavoriteCodes(): Flow<List<String>> =
        entities.map { list -> list.map { it.code } }

    override suspend fun insert(entity: FavoriteEntity) {
        entities.update { current ->
            (current.filter { it.code != entity.code } + entity).sortedBy { it.name }
        }
    }

    override suspend fun deleteByCode(code: String) {
        entities.update { it.filter { e -> e.code != code } }
    }

    override suspend fun isFavorite(code: String): Boolean =
        entities.value.any { it.code == code }
}
