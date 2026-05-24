package com.eous.mentor.features.userlibrary.presentation

data class LibraryState(
    val folders: List<String> = emptyList(),
    val isEmpty: Boolean = true
)
