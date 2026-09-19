package com.oxclub.dailynews.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.oxclub.dailynews.domain.model.Article
import com.oxclub.dailynews.domain.repository.NewsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: NewsRepository) : ViewModel() {
    private val category = MutableStateFlow("Top News")
    val articles: StateFlow<List<Article>> =
        category.flatMapLatest { repository.observeNews(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun load(value: String) {
        category.value = value
        refresh(value)
    }

    fun refresh(value: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = repository.refreshNews(value)) {
                is com.oxclub.dailynews.util.Resource.Success -> Unit
                is com.oxclub.dailynews.util.Resource.Error -> _error.value = result.message
            }
            _loading.value = false
        }
    }

    fun toggleBookmark(article: Article) {
        viewModelScope.launch { repository.toggleBookmark(article) }
    }

    companion object {
        fun factory(repo: NewsRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(repo) as T
        }
    }
}
