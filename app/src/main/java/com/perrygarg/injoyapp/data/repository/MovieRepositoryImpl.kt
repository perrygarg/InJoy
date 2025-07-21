package com.perrygarg.injoyapp.data.repository

import android.util.Log
import com.perrygarg.injoyapp.data.MovieApiService
import com.perrygarg.injoyapp.data.MovieDao
import com.perrygarg.injoyapp.data.toDomain
import com.perrygarg.injoyapp.data.toEntity
import com.perrygarg.injoyapp.domain.model.Movie
import com.perrygarg.injoyapp.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MovieRepositoryImpl(
    private val movieDao: MovieDao,
    private val movieApiService: MovieApiService
) : MovieRepository {
    override suspend fun fetchTrendingMovies(): Result<Unit> = try {
        Log.d("MovieRepository", "Fetching trending movies from API...")
        val response = movieApiService.getTrendingMovies()
        Log.d("MovieRepository", "Trending movies API response: ${response.results.size} movies")
        val entities = response.results.map { dto ->
            val existing = movieDao.getMovieById(dto.id)
            dto.toEntity("TRENDING").copy(isBookmarked = existing?.isBookmarked ?: false)
        }
        movieDao.insertMovies(entities)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("MovieRepository", "Error fetching trending movies", e)
        Result.failure(e)
    }

    override suspend fun fetchNowPlayingMovies(): Result<Unit> = try {
        Log.d("MovieRepository", "Fetching now playing movies from API...")
        val response = movieApiService.getNowPlayingMovies()
        Log.d("MovieRepository", "Now playing movies API response: ${response.results.size} movies")
        val entities = response.results.map { dto ->
            val existing = movieDao.getMovieById(dto.id)
            dto.toEntity("NOW_PLAYING").copy(isBookmarked = existing?.isBookmarked ?: false)
        }
        movieDao.insertMovies(entities)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("MovieRepository", "Error fetching now playing movies", e)
        Result.failure(e)
    }

    override fun getMoviesByCategory(category: String): Flow<List<Movie>> =
        movieDao.getMoviesByCategory(category).map { list -> list.map { it.toDomain() } }

    override suspend fun updateBookmark(movie: Movie, bookmarked: Boolean): Result<Unit> = try {
        val entity = movie.toEntity(movie.category).copy(isBookmarked = bookmarked)
        movieDao.updateMovie(entity)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
} 