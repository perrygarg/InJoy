package com.perrygarg.injoyapp.domain

import androidx.paging.PagingData
import com.perrygarg.injoyapp.domain.model.Movie
import com.perrygarg.injoyapp.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class SearchMoviesPagerUseCase(private val repository: MovieRepository) {
    operator fun invoke(query: String): Flow<PagingData<Movie>> = repository.searchMoviesPager(query)
} 