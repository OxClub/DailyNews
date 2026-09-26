package com.oxclub.dailynews.data.remote.dto
import com.google.gson.annotations.SerializedName
data class NewsResponseDto(@SerializedName("results")val results:List<ArticleDto>?)
data class ArticleDto(@SerializedName("url")val url:String?,@SerializedName("title")val title:String?,@SerializedName("description")val description:String?,@SerializedName("published_at")val publishedAt:String?,@SerializedName("sitename")val sitename:String?,@SerializedName("host")val host:String?,@SerializedName("image")val image:String?,@SerializedName("content")val content:String?)
