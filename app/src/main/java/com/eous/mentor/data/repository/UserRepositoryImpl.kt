package com.eous.mentor.data.repository

import com.eous.mentor.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient

class UserRepositoryImpl(private val supabaseClient: SupabaseClient) : UserRepository {
    override suspend fun getCurrentUser(): Result<Any> = Result.success(Any())
}
