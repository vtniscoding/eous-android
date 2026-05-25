package com.eous.mentor.data.repository

import com.eous.mentor.domain.repository.UserRepository

class UserRepositoryImpl : UserRepository {
    override suspend fun getCurrentUser(): Result<Any> = Result.success(Any())
}
