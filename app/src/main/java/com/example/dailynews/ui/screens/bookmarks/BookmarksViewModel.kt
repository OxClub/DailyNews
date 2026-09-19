package com.oxclub.dailynews.ui.screens.bookmarks

import androidx.lifecycle.*
import com.oxclub.dailynews.domain.model.Article
import com.oxclub.dailynews.domain.repository.NewsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BookmarksViewModel(private val repository: NewsRepository) : ViewModel() {
    val articles = repository.observeBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggle(article: Article) {
        viewModelScope.launch { repository.toggleBookmark(article) }
    }

    companion object {
        fun factory(repo: NewsRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                BookmarksViewModel(repo) as T
        }
    }
}
