package com.qrforge.database.dao

import androidx.room.*
import com.qrforge.database.entity.QrTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QrTemplateDao {
    @Query("SELECT * FROM qr_templates ORDER BY category, name")
    fun getAllTemplates(): Flow<List<QrTemplateEntity>>

    @Query("SELECT * FROM qr_templates WHERE category = :category ORDER BY name")
    fun getTemplatesByCategory(category: String): Flow<List<QrTemplateEntity>>

    @Query("SELECT * FROM qr_templates WHERE id = :id")
    suspend fun getById(id: Long): QrTemplateEntity?

    @Query("SELECT DISTINCT category FROM qr_templates ORDER BY category")
    fun getCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<QrTemplateEntity>)

    @Query("SELECT COUNT(*) FROM qr_templates")
    suspend fun getCount(): Int
}
