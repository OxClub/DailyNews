package com.oxclub.dailynews.domain.model
data class Article(val url:String,val title:String,val description:String?,val content:String?,val urlToImage:String?,val publishedAt:String,val sourceName:String,val category:String,val isBookmarked:Boolean=false)
