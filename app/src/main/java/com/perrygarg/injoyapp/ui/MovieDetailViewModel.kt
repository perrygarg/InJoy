package com.perrygarg.injoyapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perrygarg.injoyapp.domain.GetMovieDetailUseCase
import com.perrygarg.injoyapp.domain.UpdateBookmarkUseCase
import com.perrygarg.injoyapp.domain.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MovieDetailUiState {
    object Loading : MovieDetailUiState()
    data class Success(val movie: Movie) : MovieDetailUiState()
    data class Error(val message: String) : MovieDetailUiState()
}

class MovieDetailViewModel(
    private val getMovieDetailUseCase: GetMovieDetailUseCase,
    private val updateBookmarkUseCase: UpdateBookmarkUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<MovieDetailUiState>(MovieDetailUiState.Loading)
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    fun loadMovie(id: Int) {
        _uiState.value = MovieDetailUiState.Loading
        viewModelScope.launch {
            try {
                val movie = getMovieDetailUseCase(id)
                if (movie != null) {
                    _uiState.value = MovieDetailUiState.Success(movie)
                } else {
                    _uiState.value = MovieDetailUiState.Error("Movie not found")
                }
            } catch (e: Exception) {
                _uiState.value = MovieDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleBookmark(movie: Movie) {
        viewModelScope.launch {
            updateBookmarkUseCase(movie, !movie.isBookmarked)
            // Reload movie to update bookmark status
            loadMovie(movie.id)
        }
    }
} 