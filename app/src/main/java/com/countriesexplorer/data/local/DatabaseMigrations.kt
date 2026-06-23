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

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS profiles (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            INSERT INTO profiles (id, name, createdAt)
            VALUES (1, 'Основной', ${System.currentTimeMillis()})
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS journal_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                profileId INTEGER NOT NULL,
                countryCode TEXT,
                countryName TEXT NOT NULL,
                flagUrl TEXT NOT NULL,
                text TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS collections (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                profileId INTEGER NOT NULL,
                name TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS collection_countries (
                collectionId INTEGER NOT NULL,
                countryCode TEXT NOT NULL,
                countryName TEXT NOT NULL,
                flagUrl TEXT NOT NULL,
                addedAt INTEGER NOT NULL,
                PRIMARY KEY(collectionId, countryCode)
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS visit_history_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                profileId INTEGER NOT NULL,
                countryCode TEXT NOT NULL,
                countryName TEXT NOT NULL,
                flagUrl TEXT NOT NULL,
                visitedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            INSERT INTO visit_history_new (id, profileId, countryCode, countryName, flagUrl, visitedAt)
            SELECT id, 1, countryCode, countryName, flagUrl, visitedAt FROM visit_history
            """.trimIndent()
        )
        database.execSQL("DROP TABLE visit_history")
        database.execSQL("ALTER TABLE visit_history_new RENAME TO visit_history")
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS country_notes_new (
                profileId INTEGER NOT NULL,
                countryCode TEXT NOT NULL,
                text TEXT NOT NULL,
                updatedAt INTEGER NOT NULL,
                PRIMARY KEY(profileId, countryCode)
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            INSERT INTO country_notes_new (profileId, countryCode, text, updatedAt)
            SELECT 1, countryCode, text, updatedAt FROM country_notes
            """.trimIndent()
        )
        database.execSQL("DROP TABLE country_notes")
        database.execSQL("ALTER TABLE country_notes_new RENAME TO country_notes")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS favorites_new (
                profileId INTEGER NOT NULL,
                code TEXT NOT NULL,
                name TEXT NOT NULL,
                region TEXT NOT NULL,
                flagUrl TEXT NOT NULL,
                PRIMARY KEY(profileId, code)
            )
            """.trimIndent()
        )
        database.execSQL(
            """
            INSERT INTO favorites_new (profileId, code, name, region, flagUrl)
            SELECT 1, code, name, region, flagUrl FROM favorites
            """.trimIndent()
        )
        database.execSQL("DROP TABLE favorites")
        database.execSQL("ALTER TABLE favorites_new RENAME TO favorites")
    }
}
