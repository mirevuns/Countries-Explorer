package com.countriesexplorer.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.countriesexplorer.data.local.CollectionDao
import com.countriesexplorer.data.local.CountryNoteDao
import com.countriesexplorer.data.local.FavoriteDao
import com.countriesexplorer.data.local.JournalEntryDao
import com.countriesexplorer.data.local.ProfileDao
import com.countriesexplorer.data.local.ProfileEntity
import com.countriesexplorer.data.local.VisitHistoryDao
import com.countriesexplorer.di.ProfilePreferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao,
    private val visitHistoryDao: VisitHistoryDao,
    private val journalEntryDao: JournalEntryDao,
    private val countryNoteDao: CountryNoteDao,
    private val collectionDao: CollectionDao,
    private val favoriteDao: FavoriteDao,
    @ProfilePreferencesDataStore private val dataStore: DataStore<Preferences>
) {
    val profiles: Flow<List<ProfileEntity>> = profileDao.getAllFlow()

    val activeProfileId: Flow<Long> = dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_PROFILE_ID] ?: DEFAULT_PROFILE_ID
    }

    val activeProfile: Flow<ProfileEntity?> = combine(activeProfileId, profiles) { id, list ->
        list.find { it.id == id }
    }

    suspend fun currentProfileId(): Long = activeProfileId.first()

    suspend fun ensureDefaultProfile() {
        if (profileDao.count() == 0) {
            profileDao.insert(
                ProfileEntity(
                    name = DEFAULT_PROFILE_NAME,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        dataStore.edit { prefs ->
            val storedId = prefs[KEY_ACTIVE_PROFILE_ID]
            val profiles = profileDao.getAllFlow().first()
            val validId = storedId?.takeIf { id -> profiles.any { it.id == id } }
                ?: profiles.firstOrNull()?.id
                ?: DEFAULT_PROFILE_ID
            prefs[KEY_ACTIVE_PROFILE_ID] = validId
        }
    }

    suspend fun createProfile(name: String): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Имя профиля не может быть пустым" }
        return profileDao.insert(
            ProfileEntity(name = trimmed, createdAt = System.currentTimeMillis())
        )
    }

    suspend fun renameProfile(id: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        profileDao.getById(id)?.let { profile ->
            profileDao.insert(profile.copy(name = trimmed))
        }
    }

    suspend fun setActiveProfile(id: Long) {
        if (profileDao.getById(id) != null) {
            dataStore.edit { it[KEY_ACTIVE_PROFILE_ID] = id }
        }
    }

    suspend fun deleteProfile(id: Long) {
        val all = profileDao.getAllFlow().first()
        if (all.size <= 1) return
        visitHistoryDao.clearForProfile(id)
        journalEntryDao.clearForProfile(id)
        countryNoteDao.deleteForProfile(id)
        collectionDao.deleteCountriesForProfile(id)
        collectionDao.deleteCollectionsForProfile(id)
        favoriteDao.deleteForProfile(id)
        profileDao.deleteById(id)
        if (currentProfileId() == id) {
            val fallback = profileDao.getAllFlow().first().first().id
            setActiveProfile(fallback)
        }
    }

    companion object {
        const val DEFAULT_PROFILE_ID = 1L
        const val DEFAULT_PROFILE_NAME = "Основной"
        private val KEY_ACTIVE_PROFILE_ID = longPreferencesKey("active_profile_id")
    }
}
