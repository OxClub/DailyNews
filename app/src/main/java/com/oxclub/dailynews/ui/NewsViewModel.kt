package com.oxclub.dailynews.ui
import androidx.lifecycle.*
import com.oxclub.dailynews.domain.model.Article
import com.oxclub.dailynews.domain.repository.NewsRepository
import com.oxclub.dailynews.util.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
class NewsViewModel(private val repo:NewsRepository):ViewModel(){
 private val category=MutableStateFlow("general")
 val articles=category.flatMapLatest{repo.observeCategory(it)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 private val _trending=MutableStateFlow<List<Article>>(emptyList());val trending=_trending.asStateFlow()
 private val _search=MutableStateFlow<List<Article>>(emptyList());val searchResults=_search.asStateFlow()
 val saved=repo.observeBookmarks().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
 private val _loading=MutableStateFlow(false);val loading=_loading.asStateFlow()
 private val _error=MutableStateFlow<String?>(null);val error=_error.asStateFlow()
 private var offset=0;private var trendOffset=0
 init{refresh();loadTrending()}
 fun refresh(c:String=category.value){category.value=c;offset=0;viewModelScope.launch{_loading.value=true;when(val r=repo.refresh(c)){is Resource.Success->Unit;is Resource.Error->_error.value=r.message};_loading.value=false}}
 fun loadMore(){if(_loading.value)return;viewModelScope.launch{_loading.value=true;val n=offset+50;when(val r=repo.refresh(category.value,n)){is Resource.Success->offset=n;is Resource.Error->Unit};_loading.value=false}}
 fun loadTrending(){trendOffset=0;viewModelScope.launch{when(val r=repo.trending()){is Resource.Success->_trending.value=r.data;is Resource.Error->_error.value=r.message}}}
 fun loadMoreTrending(){if(_loading.value)return;viewModelScope.launch{_loading.value=true;val n=trendOffset+50;when(val r=repo.trending(n)){is Resource.Success->{_trending.value=(_trending.value+r.data).distinctBy{it.url};trendOffset=n};is Resource.Error->Unit};_loading.value=false}}
 fun search(q:String){if(q.isBlank()){_search.value=emptyList();return};viewModelScope.launch{when(val r=repo.search(q)){is Resource.Success->_search.value=r.data;is Resource.Error->_search.value=emptyList()}}}
 fun toggleSaved(a:Article)=viewModelScope.launch{repo.toggleBookmark(a)}
 fun isSaved(a:Article)=saved.value.any{it.url==a.url}
 companion object{fun factory(r:NewsRepository)=object:ViewModelProvider.Factory{@Suppress("UNCHECKED_CAST")override fun<T:ViewModel>create(c:Class<T>)=NewsViewModel(r) as T}}
}
