package com.perrygarg.injoyapp.domain

import com.perrygarg.injoyapp.domain.model.Movie
import com.perrygarg.injoyapp.domain.repository.MovieRepository

class UpdateBookmarkUseCase(private val repository: MovieRepository) {
    suspend operator fun invoke(movie: Movie, bookmarked: Boolean): Result<Unit> =
        repository.updateBookmark(movie, bookmarked)
} 