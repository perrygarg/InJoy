package com.perrygarg.injoyapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: Int,
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