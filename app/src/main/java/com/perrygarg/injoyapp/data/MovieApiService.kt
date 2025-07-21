package com.perrygarg.injoyapp.data

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

// Data Transfer Objects (DTOs) for TMDB API

data class MovieDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String?,
    @Json(name = "overview") val overview: String?,
    @Json(name = "release_date") val release_date: String?,
    @Json(name = "poster_path") val poster_path: String?,
    @Json(name = "backdrop_path") val backdrop_path: String?,
    @Json(name = "vote_average") val vote_average: Float?,
    @Json(name = "vote_count") val vote_count: Int?,
    @Json(name = "popularity") val popularity: Float?
)

data class MovieResponseDto(
    @Json(name = "page") val page: Int,
    @Json(name = "results") val results: List<MovieDto>,
    @Json(name = "total_pages") val total_pages: Int,
    @Json(name = "total_results") val total_results: Int
)

interface MovieApiService {
    @GET("movie/popular")
    suspend fun getTrendingMovies(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponseDto

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponseDto
} 