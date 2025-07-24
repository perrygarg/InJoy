package com.perrygarg.injoyapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.perrygarg.injoyapp.domain.model.Movie
import com.perrygarg.injoyapp.domain.usecase.SearchMoviesByTitleUseCase
import com.perrygarg.injoyapp.domain.usecase.SearchMoviesPagerUseCase
import com.perrygarg.injoyapp.domain.usecase.UpdateBookmarkUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchMoviesPagerUseCase: SearchMoviesPagerUseCase,
    private val searchMoviesByTitleUseCase: SearchMoviesByTitleUseCase,
    private val updateBookmarkUseCase: UpdateBookmarkUseCase,
    val isOffline: StateFlow<Boolean>
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val searchResults = _query
        .debounce(500)
        .flatMapLatest { q ->
            if (q.isBlank() || isOffline.value) {
                flowOf(PagingData.empty())
            } else {
                searchMoviesPagerUseCase(q)
            }
        }
        .cachedIn(viewModelScope)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PagingData.empty())

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val offlineResults = _query
        .debounce(500)
        .flatMapLatest { q ->
            if (q.isBlank() || !isOffline.value) {
                flowOf(emptyList())
            } else {
                searchMoviesByTitleUseCase(q)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isOfflineWarning: StateFlow<Boolean> = isOffline

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun toggleBookmark(movie: Movie) {
        viewModelScope.launch {
            updateBookmarkUseCase(movie, !movie.isBookmarked)
        }
    }
} 