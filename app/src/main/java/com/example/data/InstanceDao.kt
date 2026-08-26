package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InstanceDao {
    @Query("SELECT * FROM instances ORDER BY id DESC")
    fun getAllInstances(): Flow<List<Instance>>

    @Query("SELECT * FROM instances WHERE id = :id LIMIT 1")
    fun getInstanceById(id: Int): Flow<Instance?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(instance: Instance): Long

    @Delete
    suspend fun delete(instance: Instance)

    @Query("DELETE FROM instances WHERE id = :id")
    suspend fun deleteById(id: Int)
}
