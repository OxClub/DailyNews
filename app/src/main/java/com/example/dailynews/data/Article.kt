package com.example.dailynews.data

data class Article(
    val title: String,
    val description: String?,
    val image: String?,
    val url: String,
    val source: String,
    val publishedAt: String
)
