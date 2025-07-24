package com.perrygarg.injoyapp.domain.usecase

import com.perrygarg.injoyapp.domain.model.Movie
import com.perrygarg.injoyapp.domain.repository.MovieRepository
 
class GetMovieDetailUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(id: Int): Movie? = repository.getMovieById(id)
} 