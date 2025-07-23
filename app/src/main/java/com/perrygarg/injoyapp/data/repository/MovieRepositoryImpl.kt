package com.perrygarg.injoyapp.data.repository

import android.util.Log
import com.perrygarg.injoyapp.data.MovieApiService
import com.perrygarg.injoyapp.data.MovieDao
import com.perrygarg.injoyapp.data.MovieCategoryCrossRef
import com.perrygarg.injoyapp.data.toDomain
import com.perrygarg.injoyapp.data.toEntity
import com.perrygarg.injoyapp.domain.model.Movie
import com.perrygarg.injoyapp.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.map
import com.perrygarg.injoyapp.data.mediator.NowPlayingRemoteMediator
import com.perrygarg.injoyapp.data.mediator.TrendingRemoteMediator

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
            dto.toEntity().copy(isBookmarked = existing?.isBookmarked ?: false)
        }
        movieDao.insertMovies(entities)
        // Clear old cross refs for TRENDING
        movieDao.clearCategory("TRENDING")
        // Insert new cross refs with position
        val crossRefs = response.results.mapIndexed { index, dto ->
            MovieCategoryCrossRef(movieId = dto.id, category = "TRENDING", position = index)
        }
        movieDao.insertMovieCategoryCrossRefs(crossRefs)
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
            dto.toEntity().copy(isBookmarked = existing?.isBookmarked ?: false)
        }
        movieDao.insertMovies(entities)
        // Clear old cross refs for NOW_PLAYING
        movieDao.clearCategory("NOW_PLAYING")
        // Insert new cross refs with position
        val crossRefs = response.results.mapIndexed { index, dto ->
            MovieCategoryCrossRef(movieId = dto.id, category = "NOW_PLAYING", position = index)
        }
        movieDao.insertMovieCategoryCrossRefs(crossRefs)
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("MovieRepository", "Error fetching now playing movies", e)
        Result.failure(e)
    }

    override fun getMoviesByCategory(category: String): Flow<List<Movie>> =
        movieDao.getMoviesByCategory(category).map { list -> list.map { it.toDomain() } }

    override suspend fun updateBookmark(movie: Movie, bookmarked: Boolean): Result<Unit> = try {
        val entity = movie.toEntity().copy(isBookmarked = bookmarked)
        movieDao.updateMovie(entity)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getMovieById(id: Int): Movie? {
        return movieDao.getMovieById(id)?.toDomain()
    }

    override fun getBookmarkedMovies(): Flow<List<Movie>> =
        movieDao.getBookmarkedMovies().map { list -> list.map { it.toDomain() } }

    @OptIn(ExperimentalPagingApi::class)
    override fun getTrendingMoviesPager(): Flow<PagingData<Movie>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 1,
                enablePlaceholders = false
            ),
            remoteMediator = TrendingRemoteMediator(movieDao, movieApiService),
            pagingSourceFactory = { movieDao.pagingSourceByCategory("TRENDING") }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

    @OptIn(ExperimentalPagingApi::class)
    override fun getNowPlayingMoviesPager(): Flow<PagingData<Movie>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 20,
                prefetchDistance = 1,
                enablePlaceholders = false
            ),
            remoteMediator = NowPlayingRemoteMediator(movieDao, movieApiService),
            pagingSourceFactory = { movieDao.pagingSourceByCategory("NOW_PLAYING") }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
} 