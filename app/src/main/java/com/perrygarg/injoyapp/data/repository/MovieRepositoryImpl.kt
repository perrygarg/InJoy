package com.perrygarg.injoyapp.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.perrygarg.injoyapp.data.MovieApiService
import com.perrygarg.injoyapp.data.MovieDao
import com.perrygarg.injoyapp.data.mediator.NowPlayingRemoteMediator
import com.perrygarg.injoyapp.data.mediator.TrendingRemoteMediator
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