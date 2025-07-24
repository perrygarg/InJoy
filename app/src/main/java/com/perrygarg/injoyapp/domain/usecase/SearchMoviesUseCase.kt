package com.perrygarg.injoyapp.domain.usecase

import androidx.paging.PagingData
import com.perrygarg.injoyapp.domain.model.Movie
import com.perrygarg.injoyapp.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

class SearchMoviesPagerUseCase(private val repository: MovieRepository) {
    operator fun invoke(query: String): Flow<PagingData<Movie>> = repository.searchMoviesPager(query)
}

class SearchMoviesByTitleUseCase(private val repository: MovieRepository) {
    operator fun invoke(query: String): Flow<List<Movie>> = repository.searchMoviesByTitle(query)
} 