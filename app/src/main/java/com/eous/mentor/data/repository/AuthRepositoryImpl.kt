package com.eous.mentor.data.repository

import com.eous.mentor.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient

class AuthRepositoryImpl(private val supabaseClient: SupabaseClient) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun register(email: String, password: String): Result<Unit> = Result.success(Unit)
    override suspend fun logout(): Result<Unit> = Result.success(Unit)
}
