package com.eous.mentor.domain.repository

import com.eous.mentor.domain.model.Message

interface ChatRepository {
    suspend fun sendMessage(message: String): Result<Unit>
    suspend fun getMessages(): Result<List<Message>>
}
