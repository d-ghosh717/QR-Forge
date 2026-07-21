package com.qrforge.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_history")
data class QrHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "raw_data")
    val rawData: String,

    @ColumnInfo(name = "foreground_color")
    val foregroundColor: Long = 0xFF000000,

    @ColumnInfo(name = "background_color")
    val backgroundColor: Long = 0xFFFFFFFF,

    @ColumnInfo(name = "dot_style")
    val dotStyle: String = "ROUNDED",

    @ColumnInfo(name = "eye_shape")
    val eyeShape: String = "MODERN",

    @ColumnInfo(name = "logo_path")
    val logoPath: String? = null,

    @ColumnInfo(name = "frame_style")
    val frameStyle: String = "NONE",

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "qr_templates")
data class QrTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "qr_type")
    val qrType: String,

    @ColumnInfo(name = "default_data")
    val defaultData: String,

    @ColumnInfo(name = "foreground_color")
    val foregroundColor: Long,

    @ColumnInfo(name = "background_color")
    val backgroundColor: Long,

    @ColumnInfo(name = "dot_style")
    val dotStyle: String,

    @ColumnInfo(name = "eye_shape")
    val eyeShape: String,

    @ColumnInfo(name = "frame_style")
    val frameStyle: String,

    @ColumnInfo(name = "logo_path")
    val logoPath: String? = null,

    @ColumnInfo(name = "preview_icon")
    val previewIcon: String
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    @ColumnInfo(name = "key")
    val key: String,

    @ColumnInfo(name = "value")
    val value: String
)
