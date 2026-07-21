package com.qrforge.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.qrforge.database.dao.AppSettingsDao
import com.qrforge.database.dao.QrHistoryDao
import com.qrforge.database.dao.QrTemplateDao
import com.qrforge.database.entity.AppSettingsEntity
import com.qrforge.database.entity.QrHistoryEntity
import com.qrforge.database.entity.QrTemplateEntity

@Database(
    entities = [
        QrHistoryEntity::class,
        QrTemplateEntity::class,
        AppSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class QRForgeDatabase : RoomDatabase() {
    abstract fun qrHistoryDao(): QrHistoryDao
    abstract fun qrTemplateDao(): QrTemplateDao
    abstract fun appSettingsDao(): AppSettingsDao
}
