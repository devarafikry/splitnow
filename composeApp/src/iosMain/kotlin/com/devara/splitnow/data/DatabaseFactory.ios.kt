package com.devara.splitnow.data

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual fun roomDatabaseBuilder(): RoomDatabase.Builder<SplitNowDatabase> {
    val docs = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val dbUrl: NSURL = docs!!.URLByAppendingPathComponent("splitnow.db")!!
    return Room.databaseBuilder<SplitNowDatabase>(
        name = requireNotNull(dbUrl.path) { "Failed to resolve documents path for SplitNow DB" },
        factory = ::instantiateImpl,
    )
}

// Room KSP generates instantiateImpl. This stub keeps commonMain
// compilable before KSP runs.
internal fun instantiateImpl(): SplitNowDatabase = SplitNowDbConstructor.initialize()
