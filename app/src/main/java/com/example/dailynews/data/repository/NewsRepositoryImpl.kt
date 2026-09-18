package com.example.dailynews.data.repository

import com.example.dailynews.data.local.ArticleDao
import com.example.dailynews.data.local.toArticle
import com.example.dailynews.data.local.toEntity
import com.example.dailynews.data.remote.NewsApiService
import com.example.dailynews.domain.model.Article
import com.example.dailynews.domain.repository.NewsRepository
import com.example.dailynews.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

class NewsRepositoryImpl(
    private val api: NewsApiService,
    private val dao: ArticleDao
) : NewsRepository {

    override fun observeNews(category: String): Flow<List<Article>> =
        dao.getArticlesByCategory(category).map { it.map { entity -> entity.toArticle() } }

    override suspend fun refreshNews(category: String): Resource<List<Article>> {
        return try {
            val apiCategory = when (category.lowercase()) {
                "top news", "india" -> null
                else -> category.lowercase()
            }

            val response = api.getTopHeadlines(category = apiCategory)

            if (response.status != "ok") {
                return Resource.Error("News service returned an error.")
            }

            val articles = response.articles.orEmpty()
                .filter { !it.url.isNullOrBlank() && !it.title.isNullOrBlank() }
                .map { dto ->
                    Article(
                        url = dto.url!!,
                        title = dto.title!!,
                        description = dto.description,
                        content = dto.content,
                        urlToImage = dto.urlToImage,
                        publishedAt = dto.publishedAt.orEmpty(),
                        sourceName = dto.source?.name ?: "Unknown",
                        category = category,
                        isBookmarked = dao.isBookmarked(dto.url)
                    )
                }

            dao.clearNonBookmarkedByCategory(category)
            dao.insertArticles(articles.map { it.toEntity() })

            Resource.Success(articles)
        } catch (e: IOException) {
            Resource.Error("No internet connection.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unable to load news.")
        }
    }

    override suspend fun searchNews(query: String): Resource<List<Article>> {
        if (query.isBlank()) return Resource.Success(emptyList())

        return try {
            val response = api.searchNews(query = query)
            val articles = response.articles.orEmpty()
                .filter { !it.url.isNullOrBlank() && !it.title.isNullOrBlank() }
                .map { dto ->
                    Article(
                        url = dto.url!!,
                        title = dto.title!!,
                        description = dto.description,
                        content = dto.content,
                        urlToImage = dto.urlToImage,
                        publishedAt = dto.publishedAt.orEmpty(),
                        sourceName = dto.source?.name ?: "Unknown"
                    )
                }
            Resource.Success(articles)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Search failed.")
        }
    }

    override fun observeBookmarks(): Flow<List<Article>> =
        dao.getBookmarkedArticles().map { it.map { entity -> entity.toArticle() } }

    override suspend fun toggleBookmark(article: Article) {
        dao.updateBookmarkStatus(article.url, !article.isBookmarked)
    }
}
