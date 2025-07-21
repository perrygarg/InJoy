package com.perrygarg.injoyapp.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val releaseDate: String,
    val posterPath: String,
    val backdropPath: String,
    val voteAverage: Float,
    val voteCount: Int,
    val popularity: Float,
    val category: String, // "TRENDING" or "NOW_PLAYING"
    val isBookmarked: Boolean = false
)

fun Movie.copyWithBookmark(bookmarked: Boolean): Movie = this.copy(isBookmarked = bookmarked) 