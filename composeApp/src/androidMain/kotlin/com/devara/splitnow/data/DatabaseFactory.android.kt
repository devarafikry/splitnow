package com.devara.splitnow.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual fun roomDatabaseBuilder(): RoomDatabase.Builder<SplitNowDatabase> {
    val context = AndroidContextHolder.context as? Context
        ?: error("AndroidContextHolder.context not set — must be set in Application.onCreate before Koin starts.")
    val dbFile = context.getDatabasePath("splitnow.db")
    return Room.databaseBuilder<SplitNowDatabase>(
        context = context,
        name = dbFile.absolutePath,
    )
}
