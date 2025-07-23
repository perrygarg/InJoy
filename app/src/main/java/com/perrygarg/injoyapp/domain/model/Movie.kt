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
    val isBookmarked: Boolean = false
) 

