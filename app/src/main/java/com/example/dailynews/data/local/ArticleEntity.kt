package com.example.dailynews.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dailynews.domain.model.Article

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val url: String,
    val title: String,
    val description: String?,
    val content: String?,
    val urlToImage: String?,
    val publishedAt: String,
    val sourceName: String,
    val category: String,
    val isBookmarked: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis()
)

fun ArticleEntity.toArticle() = Article(
    url, title, description, content, urlToImage, publishedAt,
    sourceName, category, isBookmarked
)

fun Article.toEntity(
    category: String = this.category,
    isBookmarked: Boolean = this.isBookmarked
) = ArticleEntity(
    url = url,
    title = title,
    description = description,
    content = content,
    urlToImage = urlToImage,
    publishedAt = publishedAt,
    sourceName = sourceName,
    category = category,
    isBookmarked = isBookmarked
)
