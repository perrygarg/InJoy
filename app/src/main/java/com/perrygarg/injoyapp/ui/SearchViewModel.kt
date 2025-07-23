package com.perrygarg.injoyapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.perrygarg.injoyapp.domain.SearchMoviesPagerUseCase
import com.perrygarg.injoyapp.domain.UpdateBookmarkUseCase
import com.perrygarg.injoyapp.domain.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchMoviesPagerUseCase: SearchMoviesPagerUseCase,
    private val updateBookmarkUseCase: UpdateBookmarkUseCase
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val searchResults = _query
        .debounce(500)
        .flatMapLatest { q ->
            if (q.isBlank()) {
                kotlinx.coroutines.flow.flowOf(PagingData.empty())
            } else {
                searchMoviesPagerUseCase(q)
            }
        }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), PagingData.empty())

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun toggleBookmark(movie: Movie) {
        viewModelScope.launch {
            updateBookmarkUseCase(movie, !movie.isBookmarked)
        }
    }
} 