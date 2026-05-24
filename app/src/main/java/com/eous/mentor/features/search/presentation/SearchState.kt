package com.eous.mentor.features.search.presentation

data class SearchState(
    val searchQuery: String = "",
    val recentSearches: List<String> = emptyList()
)
