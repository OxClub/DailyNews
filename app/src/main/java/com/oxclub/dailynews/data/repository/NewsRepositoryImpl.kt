package com.oxclub.dailynews.data.repository
import com.oxclub.dailynews.data.local.*
import com.oxclub.dailynews.data.remote.*
import com.oxclub.dailynews.data.remote.dto.*
import com.oxclub.dailynews.domain.model.Article
import com.oxclub.dailynews.domain.repository.NewsRepository
import com.oxclub.dailynews.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
class NewsRepositoryImpl(private val api:NewsApiService,private val dao:ArticleDao,private val apiKey:String):NewsRepository{
 override fun observeCategory(c:String)=dao.observeCategory(c).map{it.map(ArticleEntity::toArticle)}
 override fun observeBookmarks()=dao.observeBookmarks().map{it.map(ArticleEntity::toArticle)}
 override suspend fun refresh(c:String,offset:Int):Resource<List<Article>>=request{api.search(apiKey,q=query(c),country="IN",strict=true,date="24h",sort="date",size=50,offset=offset).results.orEmpty().articles(c)}.also{if(it is Resource.Success&&offset==0){dao.clearCategory(c);dao.insertAll(it.data.map{a->a.copy(isBookmarked=dao.isSaved(a.url)).toEntity()})}}
 override suspend fun search(q:String)=request{api.search(apiKey,q=q.trim(),country=null,strict=null,date="7d",sort="relevance",size=50).results.orEmpty().articles("search")}
 override suspend fun trending(o:Int)=request{api.trends(apiKey,offset=o).results.orEmpty().flatMap{it.headlines.orEmpty()}.mapNotNull{it.article()}.distinctBy{it.url}}
 override suspend fun toggleBookmark(a:Article){dao.setSaved(a.url,!a.isBookmarked)}
 private suspend fun <T> request(b:suspend()->T):Resource<T>=try{if(apiKey.isBlank()) Resource.Error("Add NEWS_API_KEY to gradle.properties to load live news.") else Resource.Success(b())}catch(e:IOException){Resource.Error("No internet connection.")}catch(e:Exception){Resource.Error(e.message?:"Unable to load news.")}
 private fun query(c:String)=when(c.lowercase()){"top news","top","india","general"->null;"business"->"business OR economy OR markets";"technology"->"technology OR AI OR software";"sports"->"sports OR cricket";"entertainment"->"entertainment OR movies OR music";"science"->"science OR space";"health"->"health OR medicine";"world"->"world OR international";"politics"->"politics OR election OR government";else->c}
 private fun List<ArticleDto>.articles(c:String)=mapNotNull{d->val u=d.url?.trim();val t=clean(d.title);if(u.isNullOrBlank()||t.isBlank())null else Article(u,t,cleanN(d.description),cleanN(d.content),d.image?.trim()?.takeIf(String::isNotBlank),d.publishedAt.orEmpty(),clean(d.sitename).ifBlank{clean(d.host)}.ifBlank{"Unknown"},c)}.distinctBy{it.url}
 private fun TrendHeadlineDto.article():Article?{val u=url?.trim();val t=clean(title);if(u.isNullOrBlank()||t.isBlank())return null;return Article(u,t,null,null,null,publishedAt.orEmpty(),clean(sitename).ifBlank{clean(host)}.ifBlank{"Unknown"},"trending")}
 private fun clean(v:String?)=v.orEmpty().replace(Regex("<[^>]*>")," ").replace("&nbsp;"," ").replace("&amp;","&").replace("&quot;",""").replace("&#39;","'").replace("&apos;","'").replace("&lt;","<").replace("&gt;",">").replace(Regex("\s+")," ").trim()
 private fun cleanN(v:String?)=clean(v).takeIf{it.isNotBlank()}
}
