package com.qrforge.di

import android.content.Context
import androidx.room.Room
import com.qrforge.database.QRForgeDatabase
import com.qrforge.database.dao.AppSettingsDao
import com.qrforge.database.dao.QrHistoryDao
import com.qrforge.database.dao.QrTemplateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QRForgeDatabase {
        return Room.databaseBuilder(
            context,
            QRForgeDatabase::class.java,
            "qrforge.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideQrHistoryDao(db: QRForgeDatabase): QrHistoryDao = db.qrHistoryDao()

    @Provides
    fun provideQrTemplateDao(db: QRForgeDatabase): QrTemplateDao = db.qrTemplateDao()

    @Provides
    fun provideAppSettingsDao(db: QRForgeDatabase): AppSettingsDao = db.appSettingsDao()
}
