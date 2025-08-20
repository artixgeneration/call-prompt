package com.artix.callnote.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class NoteRepository private constructor(context: Context) {
    private val dao: NoteDao = AppDatabase.get(context).noteDao()

    fun observeAll(): Flow<List<Note>> = dao.observeAll()

    suspend fun save(number: String, content: String) {
        val note = Note(phoneNumber = normalize(number), content = content, updatedAt = System.currentTimeMillis())
        dao.upsert(note)
    }

    suspend fun delete(number: String) {
        dao.delete(normalize(number))
    }

    suspend fun find(number: String): Note? = dao.getByNumber(normalize(number))

    private fun normalize(number: String): String = number.filter { it.isDigit() || it == '+' }

    companion object {
        @Volatile private var INSTANCE: NoteRepository? = null
        fun get(context: Context): NoteRepository = INSTANCE ?: synchronized(this) {
            INSTANCE ?: NoteRepository(context.applicationContext).also { INSTANCE = it }
        }
    }
}
