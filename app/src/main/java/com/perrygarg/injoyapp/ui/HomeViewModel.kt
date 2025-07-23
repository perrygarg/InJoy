package com.perrygarg.injoyapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.perrygarg.injoyapp.domain.UpdateBookmarkUseCase
import com.perrygarg.injoyapp.domain.model.Movie
import com.perrygarg.injoyapp.domain.GetTrendingMoviesPagerUseCase
import com.perrygarg.injoyapp.domain.GetNowPlayingMoviesPagerUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val updateBookmarkUseCase: UpdateBookmarkUseCase,
    private val getTrendingMoviesPagerUseCase: GetTrendingMoviesPagerUseCase,
    private val getNowPlayingMoviesPagerUseCase: GetNowPlayingMoviesPagerUseCase
) : ViewModel() {

    val trendingPagingData: Flow<PagingData<Movie>> by lazy {
        getTrendingMoviesPagerUseCase().cachedIn(viewModelScope)
    }

    val nowPlayingPagingData: Flow<PagingData<Movie>> by lazy {
        getNowPlayingMoviesPagerUseCase().cachedIn(viewModelScope)
    }

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _navigationEvent = Channel<Int>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

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