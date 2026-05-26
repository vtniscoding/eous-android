package com.eous.mentor.data.repository

import com.eous.mentor.di.supabase
import com.eous.mentor.domain.model.Message
import com.eous.mentor.domain.repository.ChatRepository
import io.github.jan.supabase.postgrest.from

class ChatRepositoryImpl : ChatRepository {
    override suspend fun sendMessage(message: String): Result<Unit> {
        // Implement real sendMessage logic later if needed
        return Result.success(Unit)
    }

    override suspend fun getMessages(): Result<List<Message>> {
        return try {
            val messages = supabase.from("messages")
                .select {
                    filter {
                        eq("role", "user")
                    }
                }
                .decodeList<Message>()
            Result.success(messages)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
