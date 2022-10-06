package com.twr.mangago.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RssDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(rss:Rss)

    @Update
    suspend fun update(rss:Rss)

    @Delete
    suspend fun delete(rss:Rss)

    @Query("DELETE FROM rss_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM rss_table")
    fun getRss(): Flow<List<Rss>>
}