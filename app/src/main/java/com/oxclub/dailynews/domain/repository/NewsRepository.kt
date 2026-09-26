package com.oxclub.dailynews.domain.repository
import com.oxclub.dailynews.domain.model.Article
import com.oxclub.dailynews.util.Resource
import kotlinx.coroutines.flow.Flow
interface NewsRepository{fun observeCategory(category:String):Flow<List<Article>>;fun observeBookmarks():Flow<List<Article>>;suspend fun refresh(category:String,offset:Int=0):Resource<List<Article>>;suspend fun search(query:String):Resource<List<Article>>;suspend fun trending(offset:Int=0):Resource<List<Article>>;suspend fun toggleBookmark(article:Article)}
