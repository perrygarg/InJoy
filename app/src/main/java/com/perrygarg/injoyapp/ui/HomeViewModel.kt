package com.perrygarg.injoyapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.perrygarg.injoyapp.domain.GetNowPlayingMoviesUseCase
import com.perrygarg.injoyapp.domain.GetTrendingMoviesUseCase
import com.perrygarg.injoyapp.domain.UpdateBookmarkUseCase
import com.perrygarg.injoyapp.domain.model.Movie
import com.perrygarg.injoyapp.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed class SectionUiState<out T> {
    object Loading : SectionUiState<Nothing>()
    data class Success<T>(val data: List<T>) : SectionUiState<T>()
    data class Error(val message: String) : SectionUiState<Nothing>()
    object Empty : SectionUiState<Nothing>()
}

class HomeViewModel(
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val getNowPlayingMoviesUseCase: GetNowPlayingMoviesUseCase,
    private val getMoviesByCategory: (String) -> kotlinx.coroutines.flow.Flow<List<Movie>>,
    private val updateBookmarkUseCase: UpdateBookmarkUseCase,
    private val repository: MovieRepository
) : ViewModel() {
    private val _trendingState = MutableStateFlow<SectionUiState<Movie>>(SectionUiState.Loading)
    val trendingState: StateFlow<SectionUiState<Movie>> = _trendingState.asStateFlow()

    private val _nowPlayingState = MutableStateFlow<SectionUiState<Movie>>(SectionUiState.Loading)
    val nowPlayingState: StateFlow<SectionUiState<Movie>> = _nowPlayingState.asStateFlow()

    val trendingPagingData: Flow<PagingData<Movie>> = repository.getTrendingMoviesPager()
    val nowPlayingPagingData: Flow<PagingData<Movie>> = repository.getNowPlayingMoviesPager()

    private var trendingJob: kotlinx.coroutines.Job? = null
    private var nowPlayingJob: kotlinx.coroutines.Job? = null

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _navigationEvent = Channel<Int>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        fetchTrending()
        fetchNowPlaying()
    }

    fun fetchTrending() {
        trendingJob?.cancel()
        _trendingState.value = SectionUiState.Loading
        trendingJob = viewModelScope.launch {
            var emitted = false
            var apiFailed: Throwable? = null
            val dbFlow = getMoviesByCategory("TRENDING")
            val dbJob = launch {
                dbFlow.collectLatest { list ->
                    emitted = true
                    _trendingState.value = when {
                        list.isEmpty() && apiFailed != null -> SectionUiState.Error(apiFailed?.message ?: "Unknown error")
                        list.isEmpty() -> SectionUiState.Empty
                        else -> SectionUiState.Success(list)
                    }
                }
            }
            try {
                val apiResult = getTrendingMoviesUseCase()
                if (apiResult.isFailure) {
                    apiFailed = apiResult.exceptionOrNull()
                    // If DB has not emitted yet, wait for it; else, update state only if DB is empty
                    if (!emitted) {
                        // Wait for DB emission
                    } else if ((_trendingState.value as? SectionUiState.Success)?.data?.isEmpty() != false) {
                        _trendingState.value = SectionUiState.Error(apiFailed?.message ?: "Failed to fetch trending movies")
                    }
                }
            } catch (e: Exception) {
                apiFailed = e
                if (!emitted) {
                    // Wait for DB emission
                } else if ((_trendingState.value as? SectionUiState.Success)?.data?.isEmpty() != false) {
                    _trendingState.value = SectionUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun fetchNowPlaying() {
        nowPlayingJob?.cancel()
        _nowPlayingState.value = SectionUiState.Loading
        nowPlayingJob = viewModelScope.launch {
            var emitted = false
            var apiFailed: Throwable? = null
            val dbFlow = getMoviesByCategory("NOW_PLAYING")
            val dbJob = launch {
                dbFlow.collectLatest { list ->
                    emitted = true
                    _nowPlayingState.value = when {
                        list.isEmpty() && apiFailed != null -> SectionUiState.Error(apiFailed?.message ?: "Unknown error")
                        list.isEmpty() -> SectionUiState.Empty
                        else -> SectionUiState.Success(list)
                    }
                }
            }
            try {
                val apiResult = getNowPlayingMoviesUseCase()
                if (apiResult.isFailure) {
                    apiFailed = apiResult.exceptionOrNull()
                    if (!emitted) {
                        // Wait for DB emission
                    } else if ((_nowPlayingState.value as? SectionUiState.Success)?.data?.isEmpty() != false) {
                        _nowPlayingState.value = SectionUiState.Error(apiFailed?.message ?: "Failed to fetch now playing movies")
                    }
                }
            } catch (e: Exception) {
                apiFailed = e
                if (!emitted) {
                    // Wait for DB emission
                } else if ((_nowPlayingState.value as? SectionUiState.Success)?.data?.isEmpty() != false) {
                    _nowPlayingState.value = SectionUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun toggleBookmark(movie: Movie) {
        viewModelScope.launch {
            updateBookmarkUseCase(movie, !movie.isBookmarked)
        }
    }

    fun navigateToDetail(movie: Movie) {
        viewModelScope.launch {
            _navigationEvent.send(movie.id)
        }
    }
} 