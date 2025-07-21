package com.perrygarg.injoyapp.domain.repository

import com.perrygarg.injoyapp.domain.model.Movie
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData

interface MovieRepository {
    suspend fun fetchTrendingMovies(): Result<Unit>
    suspend fun fetchNowPlayingMovies(): Result<Unit>
    fun getMoviesByCategory(category: String): Flow<List<Movie>>
    fun getTrendingMoviesPager(): Flow<PagingData<Movie>>
    suspend fun updateBookmark(movie: Movie, bookmarked: Boolean): Result<Unit>
} 