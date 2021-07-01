package com.example.memopedia.onboarding.data

data class Category(
    val name: String = "",
    var categories: List<CategoryItem> = listOf()
)
