package com.countriesexplorer

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.countriesexplorer.data.local.AppDatabase
import com.countriesexplorer.data.local.FavoriteEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoritesRoomInstrumentedTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insert_sameCode_twice_replace_yieldsSingleRow() = runBlocking {
        val dao = db.favoriteDao()
        val c = AndroidTestFixtures.country()
        val e = FavoriteEntity.fromCountry(c)
        dao.insert(e)
        dao.insert(e)
        val all = dao.getAllFavoritesFlow().first()
        assertEquals(1, all.size)
        assertEquals(e.code, all.first().code)
    }

    @Test
    fun favoritesCodesFlow_emitsAfterInsert() = runBlocking {
        val dao = db.favoriteDao()
        withTimeout(5_000) {
            coroutineScope {
                val codesAfterInsert = async {
                    dao.getAllFavoriteCodes().first { it.contains("AA") }
                }
                launch {
                    delay(80)
                    dao.insert(FavoriteEntity.fromCountry(AndroidTestFixtures.country("A", "AA")))
                }
                assertEquals(listOf("AA"), codesAfterInsert.await())
            }
        }
    }
}
