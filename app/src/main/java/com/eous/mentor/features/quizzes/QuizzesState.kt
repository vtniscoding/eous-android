package com.eous.mentor.features.quizzes

data class QuizzesItem(val title: String, val description: String, val buttonText: String? = null)

data class QuizzesState(
        val quizzes: List<QuizzesItem> =
                listOf(
                        QuizzesItem(
                                title = "🧠 Smart Quizzes",
                                description =
                                        "Generate custom quizzes based on your recent chats. Track scores, review mistakes, and earn experience points.",
                                buttonText = "Start Quiz"
                        ),
                        QuizzesItem(
                                title = "⚡ Active Recall Flashcards",
                                description =
                                        "Automate flashcard sets from textbook screenshots or session notes. Review using spaced repetition.",
                                buttonText = null
                        )
                )
)
