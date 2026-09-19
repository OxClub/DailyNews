package com.oxclub.dailynews.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE category = :category ORDER BY publishedAt DESC")
    fun getArticlesByCategory(category: String): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY publishedAt DESC")
    fun getBookmarkedArticles(): Flow<List<ArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Query("UPDATE articles SET isBookmarked = :bookmarked WHERE url = :url")
    suspend fun updateBookmarkStatus(url: String, bookmarked: Boolean)

    @Query("SELECT EXISTS(SELECT 1 FROM articles WHERE url = :url AND isBookmarked = 1)")
    suspend fun isBookmarked(url: String): Boolean

    @Query("DELETE FROM articles WHERE isBookmarked = 0 AND category = :category")
    suspend fun clearNonBookmarkedByCategory(category: String)
}
