package com.oxclub.dailynews.data.local
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.oxclub.dailynews.domain.model.Article
@Entity(tableName="articles") data class ArticleEntity(@PrimaryKey val url:String,val title:String,val description:String?,val content:String?,val urlToImage:String?,val publishedAt:String,val sourceName:String,val category:String,val isBookmarked:Boolean=false)
fun ArticleEntity.toArticle()=Article(url,title,description,content,urlToImage,publishedAt,sourceName,category,isBookmarked)
fun Article.toEntity()=ArticleEntity(url,title,description,content,urlToImage,publishedAt,sourceName,category,isBookmarked)
