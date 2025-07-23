package com.perrygarg.injoyapp.data

import androidx.room.Entity

@Entity(primaryKeys = ["movieId", "category"])
data class MovieCategoryCrossRef(
    val movieId: Int,
    val category: String,
    val position: Int
) 