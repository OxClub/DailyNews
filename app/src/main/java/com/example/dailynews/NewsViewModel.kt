package com.example.dailynews

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailynews.data.Article
import com.example.dailynews.data.NewsApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewsViewModel(app: Application) : AndroidViewModel(app) {

    private val api = NewsApi.create(BuildConfig.NEWS_API_KEY)
    private val prefs =
        app.getSharedPreferences("dailynews", 0)

    private val gson = Gson()

    private val _articles =
        MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> =
        _articles.asStateFlow()

    private val _trending =
        MutableStateFlow<List<Article>>(emptyList())
    val trending: StateFlow<List<Article>> =
        _trending.asStateFlow()

    private val _saved =
        MutableStateFlow(loadSaved())
    val saved: StateFlow<List<Article>> =
        _saved.asStateFlow()

    private val _loading =
        MutableStateFlow(false)
    val loading: StateFlow<Boolean> =
        _loading.asStateFlow()

    private val _trendingLoading =
        MutableStateFlow(false)
    val trendingLoading: StateFlow<Boolean> =
        _trendingLoading.asStateFlow()

    private val _loadingMore =
        MutableStateFlow(false)
    val loadingMore: StateFlow<Boolean> =
        _loadingMore.asStateFlow()

    private val _error =
        MutableStateFlow<String?>(null)
    val error: StateFlow<String?> =
        _error.asStateFlow()

    private val _trendingError =
        MutableStateFlow<String?>(null)
    val trendingError: StateFlow<String?> =
        _trendingError.asStateFlow()

    private val _category =
        MutableStateFlow("general")
    val category: StateFlow<String> =
        _category.asStateFlow()

    private var currentPage = 1
    private var trendingPage = 1

    private val dismissed =
        mutableSetOf<String>()

    init {
        loadCachedHome()
        loadCachedTrending()
        refresh()
        loadTrending()
    }

    fun refresh(category: String = _category.value) {
        _category.value = category
        currentPage = 1

        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            try {
                val result =
                    api.topHeadlines(category, 1)
                        .filterNot {
                            dismissed.contains(it.url)
                        }

                _articles.value = result
                saveCache("home_$category", result)
            } catch (e: Exception) {
                if (_articles.value.isEmpty()) {
                    loadCachedHome()
                }

                _error.value =
                    e.message ?: e.javaClass.simpleName
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadMore() {
        if (_loadingMore.value) return

        viewModelScope.launch {
            _loadingMore.value = true

            try {
                val nextPage = currentPage + 1

                val result =
                    api.topHeadlines(
                        _category.value,
                        nextPage
                    ).filterNot {
                        dismissed.contains(it.url)
                    }

                if (result.isNotEmpty()) {
                    currentPage = nextPage

                    _articles.value =
                        (_articles.value + result)
                            .distinctBy { it.url }
                }
            } catch (_: Exception) {
            } finally {
                _loadingMore.value = false
            }
        }
    }

    fun loadTrending() {
        trendingPage = 1

        viewModelScope.launch {
            _trendingLoading.value = true
            _trendingError.value = null

            try {
                val result =
                    api.trending(1)
                        .filterNot {
                            dismissed.contains(it.url)
                        }

                _trending.value = result
                saveCache("trending", result)
            } catch (e: Exception) {
                if (_trending.value.isEmpty()) {
                    loadCachedTrending()
                }

                _trendingError.value =
                    e.message ?: e.javaClass.simpleName
            } finally {
                _trendingLoading.value = false
            }
        }
    }

    fun loadMoreTrending() {
        if (_loadingMore.value) return

        viewModelScope.launch {
            _loadingMore.value = true

            try {
                val nextPage = trendingPage + 1

                val result =
                    api.trending(nextPage)
                        .filterNot {
                            dismissed.contains(it.url)
                        }

                if (result.isNotEmpty()) {
                    trendingPage = nextPage

                    _trending.value =
                        (_trending.value + result)
                            .distinctBy { it.url }
                }
            } catch (_: Exception) {
            } finally {
                _loadingMore.value = false
            }
        }
    }

    fun search(
        query: String,
        onResult: (List<Article>) -> Unit
    ) {
        if (query.isBlank()) {
            onResult(emptyList())
            return
        }

        viewModelScope.launch {
            try {
                onResult(
                    api.search(query)
                        .filterNot {
                            dismissed.contains(it.url)
                        }
                )
            } catch (_: Exception) {
                onResult(emptyList())
            }
        }
    }

    fun toggleSaved(article: Article) {
        val current =
            _saved.value.toMutableList()

        val exists =
            current.any { it.url == article.url }

        if (exists) {
            current.removeAll {
                it.url == article.url
            }
        } else {
            current.add(0, article)
        }

        _saved.value = current

        prefs.edit()
            .putString(
                "saved_articles",
                gson.toJson(current)
            )
            .apply()
    }

    fun isSaved(article: Article): Boolean =
        _saved.value.any {
            it.url == article.url
        }

    fun clearSaved() {
        _saved.value = emptyList()

        prefs.edit()
            .remove("saved_articles")
            .apply()
    }

    fun dismiss(article: Article) {
        dismissed.add(article.url)

        _articles.value =
            _articles.value.filterNot {
                it.url == article.url
            }

        _trending.value =
            _trending.value.filterNot {
                it.url == article.url
            }
    }

    private fun saveCache(
        key: String,
        articles: List<Article>
    ) {
        prefs.edit()
            .putString(
                "cache_$key",
                gson.toJson(articles)
            )
            .apply()
    }

    private fun readCache(
        key: String
    ): List<Article> {
        val json =
            prefs.getString("cache_$key", null)
                ?: return emptyList()

        return try {
            val type =
                object : TypeToken<List<Article>>() {}.type

            gson.fromJson(json, type)
                ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun loadCachedHome() {
        val cached =
            readCache("home_${_category.value}")

        if (cached.isNotEmpty()) {
            _articles.value =
                cached.filterNot {
                    dismissed.contains(it.url)
                }
        }
    }

    private fun loadCachedTrending() {
        val cached =
            readCache("trending")

        if (cached.isNotEmpty()) {
            _trending.value =
                cached.filterNot {
                    dismissed.contains(it.url)
                }
        }
    }

    private fun loadSaved(): List<Article> {
        val json =
            prefs.getString("saved_articles", null)
                ?: return emptyList()

        return try {
            val type =
                object : TypeToken<List<Article>>() {}.type

            gson.fromJson(json, type)
                ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
