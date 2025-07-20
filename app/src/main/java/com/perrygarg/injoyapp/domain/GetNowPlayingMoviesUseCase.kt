package com.perrygarg.injoyapp.domain

import com.perrygarg.injoyapp.domain.repository.MovieRepository

class GetNowPlayingMoviesUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(): Result<Unit> = repository.fetchNowPlayingMovies()
} 