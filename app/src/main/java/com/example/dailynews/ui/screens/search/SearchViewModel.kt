package com.example.dailynews.ui.screens.search

import androidx.lifecycle.*
import com.example.dailynews.domain.model.Article
import com.example.dailynews.domain.repository.NewsRepository
import com.example.dailynews.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: NewsRepository) : ViewModel() {
    private val _results = MutableStateFlow<List<Article>>(emptyList())
    val results = _results.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = repository.searchNews(query.trim())) {
                is Resource.Success -> _results.value = result.data
                is Resource.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun toggle(article: Article) {
        viewModelScope.launch { repository.toggleBookmark(article) }
    }

    companion object {
        fun factory(repo: NewsRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SearchViewModel(repo) as T
        }
    }
}
