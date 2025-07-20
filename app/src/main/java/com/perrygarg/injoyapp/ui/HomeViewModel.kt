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

class HomeViewModel(
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val getNowPlayingMoviesUseCase: GetNowPlayingMoviesUseCase,
    private val getMoviesByCategory: (String) -> kotlinx.coroutines.flow.Flow<List<Movie>>
) : ViewModel() {
    private val _trendingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val trendingMovies: StateFlow<List<Movie>> = _trendingMovies.asStateFlow()

    private val _nowPlayingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val nowPlayingMovies: StateFlow<List<Movie>> = _nowPlayingMovies.asStateFlow()

    init {
        viewModelScope.launch {
            getTrendingMoviesUseCase()
        }
        viewModelScope.launch {
            getNowPlayingMoviesUseCase()
        }
        viewModelScope.launch {
            getMoviesByCategory("TRENDING").collectLatest {
                _trendingMovies.value = it
            }
        }
        viewModelScope.launch {
            getMoviesByCategory("NOW_PLAYING").collectLatest {
                _nowPlayingMovies.value = it
            }
        }
    }
} 