package com.perrygarg.injoyapp.domain.model

enum class MovieCategory(val value: String) {
    TRENDING("TRENDING"),
    NOW_PLAYING("NOW_PLAYING"),
    SEARCH("SEARCH");

    companion object {
        fun fromValue(value: String): MovieCategory? = values().find { it.value == value }
    }
} 