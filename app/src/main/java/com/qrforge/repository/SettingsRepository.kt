package com.qrforge.repository

import com.qrforge.database.dao.AppSettingsDao
import com.qrforge.database.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dao: AppSettingsDao
) {
    suspend fun getString(key: String, default: String = ""): String =
        dao.get(key)?.value ?: default

    fun getStringFlow(key: String, default: String = ""): Flow<String> =
        dao.getValueFlow(key).map { it ?: default }

    suspend fun setString(key: String, value: String) {
        dao.set(AppSettingsEntity(key, value))
    }

    suspend fun getBoolean(key: String, default: Boolean = false): Boolean =
        dao.get(key)?.value?.toBooleanStrictOrNull() ?: default

    suspend fun setBoolean(key: String, value: Boolean) {
        dao.set(AppSettingsEntity(key, value.toString()))
    }

    suspend fun getInt(key: String, default: Int = 0): Int =
        dao.get(key)?.value?.toIntOrNull() ?: default

    suspend fun setInt(key: String, value: Int) {
        dao.set(AppSettingsEntity(key, value.toString()))
    }

    suspend fun delete(key: String) {
        dao.delete(key)
    }

    companion object {
        const val KEY_DEFAULT_FG_COLOR = "default_fg_color"
        const val KEY_DEFAULT_BG_COLOR = "default_bg_color"
        const val KEY_DEFAULT_DOT_STYLE = "default_dot_style"
        const val KEY_DEFAULT_EYE_SHAPE = "default_eye_shape"
        const val KEY_EXPORT_QUALITY = "export_quality"
    }
}
