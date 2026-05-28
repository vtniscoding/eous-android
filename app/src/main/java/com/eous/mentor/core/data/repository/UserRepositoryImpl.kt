package com.eous.mentor.data.repository

import com.eous.mentor.di.supabase
import com.eous.mentor.domain.model.Bookmark
import com.eous.mentor.domain.model.Profile
import com.eous.mentor.domain.model.Quiz
import com.eous.mentor.domain.repository.UserRepository
import io.github.jan.supabase.postgrest.from

class UserRepositoryImpl : UserRepository {
    override suspend fun getProfile(userId: String): Result<Profile?> {
        return try {
            val profile = supabase.from("profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingleOrNull<Profile>()
            Result.success(profile)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override suspend fun getBookmarks(userId: String): Result<List<Bookmark>> {
        return try {
            val bookmarks = supabase.from("bookmarks")
                .select()
                .decodeList<Bookmark>()
            Result.success(bookmarks)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override suspend fun getQuizzes(userId: String): Result<List<Quiz>> {
        return try {
            val quizzes = supabase.from("quizzes")
                .select()
                .decodeList<Quiz>()
            Result.success(quizzes)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override suspend fun updateSubjects(userId: String, subjects: List<String>): Result<Unit> {
        return try {
            supabase.from("profiles").update({
                set("subjects", subjects)
            }) {
                filter { eq("id", userId) }
            }
            Result.success(Unit)
        } catch (e: Throwable) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
