package com.eous.mentor.domain.repository

interface ChatRepository {
    suspend fun sendMessage(message: String): Result<Unit>
    suspend fun getMessages(): Result<List<Any>>
}
