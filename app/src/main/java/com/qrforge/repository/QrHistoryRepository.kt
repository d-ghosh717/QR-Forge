package com.qrforge.repository

import com.qrforge.database.dao.QrHistoryDao
import com.qrforge.database.entity.QrHistoryEntity
import com.qrforge.domain.model.CustomizationOptions
import com.qrforge.domain.model.DotStyle
import com.qrforge.domain.model.EyeShape
import com.qrforge.domain.model.FrameStyle
import com.qrforge.domain.model.QrHistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QrHistoryRepository @Inject constructor(
    private val dao: QrHistoryDao
) {
    fun getAllHistory(): Flow<List<QrHistoryItem>> =
        dao.getAllHistory().map { list -> list.map { it.toDomain() } }

    fun getRecentHistory(limit: Int = 10): Flow<List<QrHistoryItem>> =
        dao.getRecentHistory(limit).map { list -> list.map { it.toDomain() } }

    fun getFavorites(): Flow<List<QrHistoryItem>> =
        dao.getFavorites().map { list -> list.map { it.toDomain() } }

    fun getById(id: Long): Flow<QrHistoryItem?> =
        dao.getByIdFlow(id).map { it?.toDomain() }

    suspend fun getByIdOnce(id: Long): QrHistoryItem? =
        dao.getById(id)?.toDomain()

    fun searchHistory(query: String): Flow<List<QrHistoryItem>> =
        dao.searchHistory(query).map { list -> list.map { it.toDomain() } }

    suspend fun insert(item: QrHistoryItem): Long {
        val entity = item.toEntity()
        return dao.insert(entity)
    }

    suspend fun update(item: QrHistoryItem) {
        dao.update(item.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        dao.toggleFavorite(id, isFavorite)
    }

    suspend fun rename(id: Long, title: String) {
        dao.rename(id, title)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun getCount(): Int = dao.getCount()

    private fun QrHistoryEntity.toDomain() = QrHistoryItem(
        id = id,
        type = type,
        title = title,
        content = content,
        rawData = rawData,
        foregroundColor = foregroundColor,
        backgroundColor = backgroundColor,
        dotStyle = dotStyle,
        eyeShape = eyeShape,
        logoPath = logoPath,
        frameStyle = frameStyle,
        isFavorite = isFavorite,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun QrHistoryItem.toEntity() = QrHistoryEntity(
        id = id,
        type = type,
        title = title,
        content = content,
        rawData = rawData,
        foregroundColor = foregroundColor,
        backgroundColor = backgroundColor,
        dotStyle = dotStyle,
        eyeShape = eyeShape,
        logoPath = logoPath,
        frameStyle = frameStyle,
        isFavorite = isFavorite,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
