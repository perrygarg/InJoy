package com.perrygarg.injoyapp.data.local

import androidx.room.Entity

@Entity(primaryKeys = ["movieId", "category"])
data class MovieCategoryCrossRef(
    val movieId: Int,
    val category: String,
    val position: Int
) 