package com.countriesexplorer.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS favorites_new (
                code TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                region TEXT NOT NULL,
                flagUrl TEXT NOT NULL
            )
            """.trimIndent()
        )
        database.execSQL("DROP TABLE IF EXISTS favorites")
        database.execSQL("ALTER TABLE favorites_new RENAME TO favorites")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cached_countries (
                code TEXT NOT NULL PRIMARY KEY,
                country TEXT NOT NULL,
                region TEXT NOT NULL,
                cachedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cache_metadata (
                id INTEGER NOT NULL PRIMARY KEY,
                lastFullSyncAt INTEGER,
                lastSyncStatus TEXT NOT NULL
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS visit_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                countryCode TEXT NOT NULL,
                countryName TEXT NOT NULL,
                flagUrl TEXT NOT NULL,
                visitedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS country_notes (
                countryCode TEXT NOT NULL PRIMARY KEY,
                text TEXT NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            INSERT INTO cache_metadata (id, lastFullSyncAt, lastSyncStatus)
            VALUES (1, NULL, 'unknown')
            """.trimIndent()
        )
    }
}
