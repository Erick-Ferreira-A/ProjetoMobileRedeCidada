package com.example.projetorevisao.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incidents ORDER BY createdAtEpochMs DESC, rowid DESC")
    fun observeAll(): Flow<List<IncidentEntity>>

    @Query("SELECT COUNT(*) FROM incidents")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(incident: IncidentEntity)

    @Query("DELETE FROM incidents WHERE id = :id")
    suspend fun deleteById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(incidents: List<IncidentEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(incidents: List<IncidentEntity>)

    /** Tira as cópias de relatos que o servidor não devolve mais (apagados, denunciados ou bloqueados). */
    @Query("DELETE FROM incidents WHERE fromServer = 1 AND id NOT IN (:keep)")
    suspend fun deleteServerCopiesExcept(keep: List<String>)

    @Query("DELETE FROM incidents WHERE fromServer = 1")
    suspend fun deleteAllServerCopies()
}
