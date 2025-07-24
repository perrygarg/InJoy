package com.perrygarg.injoyapp.domain.model

enum class MovieCategory(val value: String) {
    TRENDING("TRENDING"),
    NOW_PLAYING("NOW_PLAYING");

    companion object {
        fun fromValue(value: String): MovieCategory? = MovieCategory.entries.find { it.value == value }
    }
} 