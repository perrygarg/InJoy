package com.perrygarg.injoyapp.domain.repository

import androidx.paging.PagingData
import com.perrygarg.injoyapp.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getTrendingMoviesPager(): Flow<PagingData<Movie>>
    fun getNowPlayingMoviesPager(): Flow<PagingData<Movie>>
    suspend fun updateBookmark(movie: Movie, bookmarked: Boolean): Result<Unit>
    suspend fun getMovieById(id: Int): Movie?
    fun getBookmarkedMovies(): Flow<List<Movie>>
} 