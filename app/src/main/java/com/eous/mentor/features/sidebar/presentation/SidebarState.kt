package com.eous.mentor.features.sidebar.presentation

data class SidebarState(
    val isSidebarOpen: Boolean = false,
    val currentScreen: String = "dashboard",
    val chatInitialQuestion: String = "",
    val recentChats: List<String> = listOf(
        "how i can draw squre...",
        "Explain Photosynthesis in ..."
    )
)
