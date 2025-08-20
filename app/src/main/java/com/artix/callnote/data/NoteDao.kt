package com.artix.callnote.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Upsert
    suspend fun upsert(note: Note)

    @Query("SELECT * FROM notes WHERE phoneNumber = :number LIMIT 1")
    suspend fun getByNumber(number: String): Note?

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<Note>>

    @Query("DELETE FROM notes WHERE phoneNumber = :number")
    suspend fun delete(number: String)
}
