package com.perrygarg.injoyapp.domain.usecase

import androidx.paging.PagingData
import com.perrygarg.injoyapp.domain.model.Movie
import com.perrygarg.injoyapp.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class GetTrendingMoviesPagerUseCase(private val repository: MovieRepository) {
    operator fun invoke(): Flow<PagingData<Movie>> = repository.getTrendingMoviesPager()
} 