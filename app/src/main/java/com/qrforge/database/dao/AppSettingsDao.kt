package com.qrforge.database.dao

import androidx.room.*
import com.qrforge.database.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE `key` = :key")
    suspend fun get(key: String): AppSettingsEntity?

    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    fun getValueFlow(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(setting: AppSettingsEntity)

    @Query("DELETE FROM app_settings WHERE `key` = :key")
    suspend fun delete(key: String)
}
