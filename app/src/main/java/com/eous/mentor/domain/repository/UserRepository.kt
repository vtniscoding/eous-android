package com.eous.mentor.domain.repository

import com.eous.mentor.domain.model.Bookmark
import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.model.Quiz

interface UserRepository {
    suspend fun getProfile(userId: String): Result<Profile?>
    suspend fun getBookmarks(userId: String): Result<List<Bookmark>>
    suspend fun getQuizzes(userId: String): Result<List<Quiz>>
}
