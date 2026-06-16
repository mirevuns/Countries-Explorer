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
