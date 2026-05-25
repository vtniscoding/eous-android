package com.eous.mentor.data.repository

import com.eous.mentor.domain.repository.ChatRepository
import io.github.jan.supabase.SupabaseClient

class ChatRepositoryImpl(private val supabaseClient: SupabaseClient) : ChatRepository {
    override suspend fun sendMessage(message: String): Result<Unit> = Result.success(Unit)
    override suspend fun getMessages(): Result<List<Any>> = Result.success(emptyList())
}
