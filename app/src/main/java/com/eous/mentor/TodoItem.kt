package com.eous.mentor

import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(val id: Int, val name: String)
