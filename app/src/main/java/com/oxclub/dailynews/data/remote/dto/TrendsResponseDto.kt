package com.oxclub.dailynews.data.remote.dto
import com.google.gson.annotations.SerializedName
data class TrendsResponseDto(@SerializedName("results")val results:List<TrendDto>?)
data class TrendDto(@SerializedName("headlines")val headlines:List<TrendHeadlineDto>?)
data class TrendHeadlineDto(@SerializedName("title")val title:String?,@SerializedName("url")val url:String?,@SerializedName("published_at")val publishedAt:String?,@SerializedName("host")val host:String?,@SerializedName("sitename")val sitename:String?)
