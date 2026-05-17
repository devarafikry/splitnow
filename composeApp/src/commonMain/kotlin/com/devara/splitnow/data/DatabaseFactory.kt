package com.devara.splitnow.data

import androidx.room.RoomDatabase

/** Platform-specific Room builder bridge — Android needs Context, iOS needs file path. */
expect fun roomDatabaseBuilder(): RoomDatabase.Builder<SplitNowDatabase>
