package com.zenith.focus

import android.content.Context
import com.zenith.focus.data.local.ZenithDatabaseHelper
import com.zenith.focus.data.repository.LockRepositoryImpl
import com.zenith.focus.data.repository.SettingsRepositoryImpl
import com.zenith.focus.data.repository.StatisticsRepositoryImpl
import com.zenith.focus.domain.repository.LockRepository
import com.zenith.focus.domain.repository.SettingsRepository
import com.zenith.focus.domain.repository.StatisticsRepository

class ZenithAppContainer(context: Context) {
    val databaseHelper: ZenithDatabaseHelper by lazy {
        ZenithDatabaseHelper(context.applicationContext)
    }

    val lockRepository: LockRepository by lazy {
        LockRepositoryImpl(context.applicationContext)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImpl(context.applicationContext)
    }

    val statisticsRepository: StatisticsRepository by lazy {
        StatisticsRepositoryImpl(databaseHelper)
    }

    val nuclearModeRepository: com.zenith.focus.domain.nuclear.NuclearModeRepository by lazy {
        com.zenith.focus.data.nuclear.NuclearModeRepositoryImpl(context.applicationContext)
    }

    val updateManager: com.zenith.focus.core.update.UpdateManager by lazy {
        com.zenith.focus.core.update.UpdateManager(context.applicationContext)
    }

    val permissionOrchestrator: com.zenith.focus.core.permission.PermissionOrchestrator by lazy {
        com.zenith.focus.core.permission.PermissionOrchestrator(context.applicationContext)
    }
}
