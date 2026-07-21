package com.qrforge.database.dao

import androidx.room.*
import com.qrforge.database.entity.QrHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QrHistoryDao {
    @Query("SELECT * FROM qr_history ORDER BY updated_at DESC")
    fun getAllHistory(): Flow<List<QrHistoryEntity>>

    @Query("SELECT * FROM qr_history ORDER BY updated_at DESC LIMIT :limit")
    fun getRecentHistory(limit: Int = 10): Flow<List<QrHistoryEntity>>

    @Query("SELECT * FROM qr_history WHERE is_favorite = 1 ORDER BY updated_at DESC")
    fun getFavorites(): Flow<List<QrHistoryEntity>>

    @Query("SELECT * FROM qr_history WHERE id = :id")
    suspend fun getById(id: Long): QrHistoryEntity?

    @Query("SELECT * FROM qr_history WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<QrHistoryEntity?>

    @Query(
        "SELECT * FROM qr_history WHERE " +
        "title LIKE '%' || :query || '%' OR " +
        "type LIKE '%' || :query || '%' OR " +
        "content LIKE '%' || :query || '%' " +
        "ORDER BY updated_at DESC"
    )
    fun searchHistory(query: String): Flow<List<QrHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: QrHistoryEntity): Long

    @Update
    suspend fun update(entity: QrHistoryEntity)

    @Query("UPDATE qr_history SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE qr_history SET title = :title WHERE id = :id")
    suspend fun rename(id: Long, title: String)

    @Query("DELETE FROM qr_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM qr_history")
    suspend fun getCount(): Int
}
