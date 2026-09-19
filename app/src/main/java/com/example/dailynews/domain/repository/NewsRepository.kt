package com.oxclub.dailynews.domain.repository

import com.oxclub.dailynews.domain.model.Article
import com.oxclub.dailynews.util.Resource
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun observeNews(category: String): Flow<List<Article>>
    suspend fun refreshNews(category: String): Resource<List<Article>>
    suspend fun searchNews(query: String): Resource<List<Article>>
    fun observeBookmarks(): Flow<List<Article>>
    suspend fun toggleBookmark(article: Article)
}
