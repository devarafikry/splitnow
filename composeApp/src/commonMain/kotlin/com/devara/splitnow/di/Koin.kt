package com.devara.splitnow.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.devara.splitnow.ai.GeminiClient
import com.devara.splitnow.ai.SplitParser
import com.devara.splitnow.data.SettingsStore
import com.devara.splitnow.data.SplitNowDatabase
import com.devara.splitnow.data.SplitRepository
import com.devara.splitnow.data.roomDatabaseBuilder
import com.devara.splitnow.l10n.Translator
import com.devara.splitnow.platform.UrlOpener
import com.devara.splitnow.scan.TextRecognizer
import com.devara.splitnow.share.SharePngLauncher
import com.devara.splitnow.ui.flow.SplitFlowState
import kotlinx.coroutines.Dispatchers
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(commonModule)
    }
}

private val commonModule = module {
    single<SplitNowDatabase> {
        roomDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.Default)
            // Dev-mode migration policy — schema can still churn before launch.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single { SplitRepository(get()) }
    single { SettingsStore() }
    single { GeminiClient(get(), geminiApiKey()) }
    single { SplitParser(get()) }
    single { TextRecognizer() }
    single { SharePngLauncher() }
    single { SplitFlowState() }
    single { Translator(get(), get()) }
    single { UrlOpener() }
}
