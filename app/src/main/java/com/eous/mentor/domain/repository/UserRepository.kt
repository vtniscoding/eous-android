package com.eous.mentor.domain.repository

interface UserRepository {
    suspend fun getCurrentUser(): Result<Any>
}
