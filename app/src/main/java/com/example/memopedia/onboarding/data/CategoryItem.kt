package com.example.memopedia.onboarding.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class CategoryItem(
    val name: String = "",
    val subreddit: String = "",
    val tags: List<String> = listOf(),
    val url: String = "",
    @get:Exclude
    val isSelected: Boolean = false
)