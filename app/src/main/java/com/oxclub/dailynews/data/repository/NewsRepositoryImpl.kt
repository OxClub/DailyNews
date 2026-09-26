package com.oxclub.dailynews.data.repository

import com.oxclub.dailynews.data.local.ArticleDao
import com.oxclub.dailynews.data.local.ArticleEntity
import com.oxclub.dailynews.data.local.toArticle
import com.oxclub.dailynews.data.local.toEntity
import com.oxclub.dailynews.data.remote.NewsApiService
import com.oxclub.dailynews.data.remote.dto.ArticleDto
import com.oxclub.dailynews.data.remote.dto.TrendHeadlineDto
import com.oxclub.dailynews.domain.model.Article
import com.oxclub.dailynews.domain.repository.NewsRepository
import com.oxclub.dailynews.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

class NewsRepositoryImpl(
    private val api: NewsApiService,
    private val dao: ArticleDao
) : NewsRepository {

    override fun observeCategory(category: String): Flow<List<Article>> =
        dao.observeCategory(category).map { items ->
            items.map(ArticleEntity::toArticle)
        }

    override fun observeBookmarks(): Flow<List<Article>> =
        dao.observeBookmarks().map { items ->
            items.map(ArticleEntity::toArticle)
        }

    override suspend fun refresh(
        category: String,
        offset: Int
    ): Resource<List<Article>> =
        request {
            api.search(
                q = query(category),
                country = "IN",
                strict = true,
                date = "24h",
                sort = "date",
                size = 50,
                offset = offset
            ).results.orEmpty().articles(category)
        }.also { result ->
            if (result is Resource.Success && offset == 0) {
                dao.clearCategory(category)
                dao.insertAll(
                    result.data.map { article ->
                        article.copy(
                            isBookmarked = dao.isSaved(article.url)
                        ).toEntity()
                    }
                )
            }
        }

    override suspend fun search(q: String): Resource<List<Article>> =
        request {
            api.search(
                q = q.trim(),
                country = null,
                strict = null,
                date = "7d",
                sort = "relevance",
                size = 50
            ).results.orEmpty().articles("search")
        }

    override suspend fun trending(offset: Int): Resource<List<Article>> =
        request {
            api.trends(offset = offset)
                .results
                .orEmpty()
                .flatMap { it.headlines.orEmpty() }
                .mapNotNull { it.article() }
                .distinctBy { it.url }
        }

    override suspend fun toggleBookmark(article: Article) {
        dao.setSaved(article.url, !article.isBookmarked)
    }

    private suspend fun <T> request(
        block: suspend () -> T
    ): Resource<T> {
        return try {
            Resource.Success(block())
        } catch (_: IOException) {
            Resource.Error("No internet connection.")
        } catch (error: Exception) {
            Resource.Error(
                error.message ?: "Unable to load news."
            )
        }
    }

    private fun query(category: String): String? =
        when (category.lowercase()) {
            "top news", "top", "india", "general" -> null
            "business" -> "business OR economy OR markets"
            "technology" -> "technology OR AI OR software"
            "sports" -> "sports OR cricket"
            "entertainment" -> "entertainment OR movies OR music"
            "science" -> "science OR space"
            "health" -> "health OR medicine"
            "world" -> "world OR international"
            "politics" -> "politics OR election OR government"
            else -> category
        }

    private fun List<ArticleDto>.articles(
        category: String
    ): List<Article> {
        return mapNotNull { dto ->
            val url = dto.url?.trim()
            val title = clean(dto.title)

            if (url.isNullOrBlank() || title.isBlank()) {
                null
            } else {
                Article(
                    url = url,
                    title = title,
                    description = cleanNullable(dto.description),
                    content = cleanNullable(dto.content),
                    urlToImage = dto.image
                        ?.trim()
                        ?.takeIf(String::isNotBlank),
                    publishedAt = dto.publishedAt.orEmpty(),
                    sourceName = clean(dto.sitename)
                        .ifBlank { clean(dto.host) }
                        .ifBlank { "Unknown" },
                    category = category
                )
            }
        }.distinctBy { it.url }
    }

    private fun TrendHeadlineDto.article(): Article? {
        val articleUrl = url?.trim()
        val articleTitle = clean(title)

        if (articleUrl.isNullOrBlank() || articleTitle.isBlank()) {
            return null
        }

        return Article(
            url = articleUrl,
            title = articleTitle,
            description = null,
            content = null,
            urlToImage = null,
            publishedAt = publishedAt.orEmpty(),
            sourceName = clean(sitename)
                .ifBlank { clean(host) }
                .ifBlank { "Unknown" },
            category = "trending"
        )
    }

    private fun clean(value: String?): String {
        return value.orEmpty()
            .replace(Regex("<[^>]*>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun cleanNullable(value: String?): String? {
        return clean(value).takeIf { it.isNotBlank() }
    }
}
