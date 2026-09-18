package com.example.dailynews

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailynews.data.Article
import com.example.dailynews.data.NewsApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewsViewModel(app: Application) : AndroidViewModel(app) {
    private val api = NewsApi.create(BuildConfig.NEWS_API_KEY)
    private val prefs = app.getSharedPreferences("saved", 0)

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _saved = MutableStateFlow<List<Article>>(emptyList())
    val saved: StateFlow<List<Article>> = _saved.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _category = MutableStateFlow("general")
    val category: StateFlow<String> = _category.asStateFlow()

    init {
        refresh()
    }

    fun refresh(category: String = _category.value) {
        _category.value = category
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _articles.value = api.topHeadlines(category)
            } catch (e: Exception) {
                _error.value = "Unable to load news. Check your internet connection."
            } finally {
                _loading.value = false
            }
        }
    }

    fun search(query: String, onResult: (List<Article>) -> Unit) {
        viewModelScope.launch {
            try {
                onResult(api.search(query))
            } catch (_: Exception) {
                onResult(emptyList())
            }
        }
    }

    fun toggleSaved(article: Article) {
        val key = article.url
        val nowSaved = !prefs.getBoolean(key, false)
        prefs.edit().putBoolean(key, nowSaved).apply()

        if (nowSaved) {
            _saved.value = (_saved.value + article).distinctBy { it.url }
        } else {
            _saved.value = _saved.value.filterNot { it.url == key }
        }
    }

    fun isSaved(article: Article): Boolean = prefs.getBoolean(article.url, false)
}
