package com.eous.mentor.features.userlibrary

data class LibraryState(
    val folders: List<String> = emptyList(),
    val isEmpty: Boolean = true
)
