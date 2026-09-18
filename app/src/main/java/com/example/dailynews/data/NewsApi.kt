package com.example.dailynews.data

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

class NewsApi(private val key: String) {

    private val client = OkHttpClient()

    suspend fun topHeadlines(category: String): List<Article> =
        request(
            "https://newsapi.org/v2/top-headlines?country=in&pageSize=50&apiKey=$key" +
                if (category != "general") "&category=$category" else ""
        )

    suspend fun search(query: String): List<Article> =
        request(
            "https://newsapi.org/v2/everything?q=${URLEncoder.encode(query, "UTF-8")}" +
                "&language=en&pageSize=50&sortBy=publishedAt&apiKey=$key"
        )

    private suspend fun request(url: String): List<Article> =
        withContext(Dispatchers.IO) {
            require(key.isNotBlank()) { "NEWS_API_KEY is missing" }

            val response = client.newCall(
                Request.Builder()
                    .url(url)
                    .build()
            ).execute()

            response.use {
                if (!it.isSuccessful) {
                    error("HTTP ${it.code}")
                }

                val body = it.body?.string()
                    ?: error("Empty response")

                val dto = Gson().fromJson(
                    body,
                    NewsResponse::class.java
                )

                if (dto.status != "ok") {
                    error(dto.message ?: "News API error")
                }

                dto.articles.orEmpty()
                    .filter {
                        !it.url.isNullOrBlank() &&
                        !it.title.isNullOrBlank()
                    }
                    .map {
                        Article(
                            title = it.title!!,
                            description = it.description,
                            image = it.urlToImage,
                            url = it.url!!,
                            source = it.source?.name ?: "News",
                            publishedAt = it.publishedAt.orEmpty()
                        )
                    }
            }
        }

    companion object {
        fun create(key: String) = NewsApi(key)
    }
}

data class NewsResponse(
    val status: String,
    val message: String?,
    val articles: List<ArticleDto>?
)

data class ArticleDto(
    val title: String?,
    val description: String?,
    val urlToImage: String?,
    val url: String?,
    val publishedAt: String?,
    val source: SourceDto?
)

data class SourceDto(
    val name: String?
)
