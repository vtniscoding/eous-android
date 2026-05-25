package com.eous.mentor.features.sidebar

data class SidebarState(
    val isSidebarOpen: Boolean = false,
    val currentScreen: String = "dashboard",
    val chatInitialQuestion: String = "",
    val recentChats: List<String> = listOf(
        "how i can draw squre...",
        "Explain Photosynthesis in ..."
    )
)
