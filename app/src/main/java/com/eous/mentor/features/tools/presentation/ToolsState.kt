package com.eous.mentor.features.tools.presentation

data class ToolItem(
    val title: String,
    val description: String,
    val buttonText: String? = null
)

data class ToolsState(
    val tools: List<ToolItem> = listOf(
        ToolItem(
            title = "🧠 Smart Quizzes",
            description = "Generate custom quizzes based on your recent chats. Track scores, review mistakes, and earn experience points.",
            buttonText = "Start Quiz"
        ),
        ToolItem(
            title = "⚡ Active Recall Flashcards",
            description = "Automate flashcard sets from textbook screenshots or session notes. Review using spaced repetition.",
            buttonText = null
        )
    )
)
