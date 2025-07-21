package com.perrygarg.injoyapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perrygarg.injoyapp.domain.GetTrendingMoviesUseCase
import com.perrygarg.injoyapp.domain.GetNowPlayingMoviesUseCase
import com.perrygarg.injoyapp.domain.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

sealed class SectionUiState<out T> {
    object Loading : SectionUiState<Nothing>()
    data class Success<T>(val data: List<T>) : SectionUiState<T>()
    data class Error(val message: String) : SectionUiState<Nothing>()
    object Empty : SectionUiState<Nothing>()
}

class HomeViewModel(
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val getNowPlayingMoviesUseCase: GetNowPlayingMoviesUseCase,
    private val getMoviesByCategory: (String) -> kotlinx.coroutines.flow.Flow<List<Movie>>
) : ViewModel() {
    private val _trendingState = MutableStateFlow<SectionUiState<Movie>>(SectionUiState.Loading)
    val trendingState: StateFlow<SectionUiState<Movie>> = _trendingState.asStateFlow()

    private val _nowPlayingState = MutableStateFlow<SectionUiState<Movie>>(SectionUiState.Loading)
    val nowPlayingState: StateFlow<SectionUiState<Movie>> = _nowPlayingState.asStateFlow()

    private var trendingJob: kotlinx.coroutines.Job? = null
    private var nowPlayingJob: kotlinx.coroutines.Job? = null

    init {
        fetchTrending()
        fetchNowPlaying()
    }

    fun fetchTrending() {
        trendingJob?.cancel()
        _trendingState.value = SectionUiState.Loading
        trendingJob = viewModelScope.launch {
            try {
                val apiResult = getTrendingMoviesUseCase()
                if (apiResult.isFailure) {
                    _trendingState.value = SectionUiState.Error(apiResult.exceptionOrNull()?.message ?: "Failed to fetch trending movies")
                    return@launch
                }
                getMoviesByCategory("TRENDING").collectLatest { list ->
                    _trendingState.value = when {
                        list.isEmpty() -> SectionUiState.Empty
                        else -> SectionUiState.Success(list)
                    }
                }
            } catch (e: CancellationException) {
                // ignore
            } catch (e: Exception) {
                _trendingState.value = SectionUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun fetchNowPlaying() {
        nowPlayingJob?.cancel()
        _nowPlayingState.value = SectionUiState.Loading
        nowPlayingJob = viewModelScope.launch {
            try {
                val apiResult = getNowPlayingMoviesUseCase()
                if (apiResult.isFailure) {
                    _nowPlayingState.value = SectionUiState.Error(apiResult.exceptionOrNull()?.message ?: "Failed to fetch now playing movies")
                    return@launch
                }
                getMoviesByCategory("NOW_PLAYING").collectLatest { list ->
                    _nowPlayingState.value = when {
                        list.isEmpty() -> SectionUiState.Empty
                        else -> SectionUiState.Success(list)
                    }
                }
            } catch (e: CancellationException) {
                // ignore
            } catch (e: Exception) {
                _nowPlayingState.value = SectionUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
} 