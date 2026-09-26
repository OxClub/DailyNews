package com.oxclub.dailynews.data.local
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao interface ArticleDao{
 @Query("SELECT * FROM articles WHERE category=:category ORDER BY publishedAt DESC") fun observeCategory(category:String):Flow<List<ArticleEntity>>
 @Query("SELECT * FROM articles WHERE isBookmarked=1 ORDER BY publishedAt DESC") fun observeBookmarks():Flow<List<ArticleEntity>>
 @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun insertAll(items:List<ArticleEntity>)
 @Query("DELETE FROM articles WHERE isBookmarked=0 AND category=:category") suspend fun clearCategory(category:String)
 @Query("UPDATE articles SET isBookmarked=:saved WHERE url=:url") suspend fun setSaved(url:String,saved:Boolean)
 @Query("SELECT EXISTS(SELECT 1 FROM articles WHERE url=:url AND isBookmarked=1)") suspend fun isSaved(url:String):Boolean
}
