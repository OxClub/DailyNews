package com.example.dailynews.data.remote

import com.example.dailynews.BuildConfig
import com.example.dailynews.data.remote.dto.NewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "in",
        @Query("category") category: String? = null,
        @Query("pageSize") pageSize: Int = 50,
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY
    ): NewsResponseDto

    @GET("v2/everything")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 50,
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY
    ): NewsResponseDto
}
