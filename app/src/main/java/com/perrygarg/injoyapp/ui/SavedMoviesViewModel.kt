package com.perrygarg.injoyapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perrygarg.injoyapp.domain.GetBookmarkedMoviesUseCase
import com.perrygarg.injoyapp.domain.UpdateBookmarkUseCase
import com.perrygarg.injoyapp.domain.model.Movie
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedMoviesViewModel(
    getBookmarkedMoviesUseCase: GetBookmarkedMoviesUseCase,
    private val updateBookmarkUseCase: UpdateBookmarkUseCase
) : ViewModel() {
    val bookmarkedMovies: StateFlow<List<Movie>> = getBookmarkedMoviesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleBookmark(movie: Movie) {
        viewModelScope.launch {
            updateBookmarkUseCase(movie, false)
        }
    }
} 