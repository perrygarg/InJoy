package com.perrygarg.injoyapp.data

import com.perrygarg.injoyapp.domain.model.Movie

fun MovieDto.toEntity(): MovieEntity = MovieEntity(
    id = this.id,
    title = this.title ?: "",
    overview = this.overview ?: "",
    releaseDate = this.release_date ?: "",
    posterPath = this.poster_path ?: "",
    backdropPath = this.backdrop_path ?: "",
    voteAverage = this.vote_average ?: 0f,
    voteCount = this.vote_count ?: 0,
    popularity = this.popularity ?: 0f
)

fun MovieEntity.toDomain(): Movie = Movie(
    id = this.id,
    title = this.title,
    overview = this.overview,
    releaseDate = this.releaseDate,
    posterPath = this.posterPath,
    backdropPath = this.backdropPath,
    voteAverage = this.voteAverage,
    voteCount = this.voteCount,
    popularity = this.popularity,
    isBookmarked = this.isBookmarked
)

fun Movie.toEntity(): MovieEntity = MovieEntity(
    id = this.id,
    title = this.title,
    overview = this.overview,
    releaseDate = this.releaseDate,
    posterPath = this.posterPath,
    backdropPath = this.backdropPath,
    voteAverage = this.voteAverage,
    voteCount = this.voteCount,
    popularity = this.popularity,
    isBookmarked = this.isBookmarked
) 