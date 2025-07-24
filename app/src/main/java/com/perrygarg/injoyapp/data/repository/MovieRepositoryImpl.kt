package com.perrygarg.injoyapp.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.map
import com.perrygarg.injoyapp.data.local.MovieDao
import com.perrygarg.injoyapp.data.local.MovieEntity
import com.perrygarg.injoyapp.data.mapper.toDomain
import com.perrygarg.injoyapp.data.mapper.toEntity
import com.perrygarg.injoyapp.data.mediator.NowPlayingRemoteMediator
import com.perrygarg.injoyapp.data.mediator.TrendingRemoteMediator
import com.perrygarg.injoyapp.data.remote.MovieApiService
import com.perrygarg.injoyapp.domain.model.Movie
import com.perrygarg.injoyapp.domain.model.MovieCategory
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
            pagingSourceFactory = { movieDao.pagingSourceByCategory(MovieCategory.TRENDING.value) }
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
            pagingSourceFactory = { movieDao.pagingSourceByCategory(MovieCategory.NOW_PLAYING.value) }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override fun searchMoviesPager(query: String): Flow<PagingData<Movie>> =
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false, prefetchDistance = 1),
            pagingSourceFactory = {
                object : PagingSource<Int, MovieEntity>() {
                    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieEntity> {
                        val page = params.key ?: 1
                        return try {
                            val response = movieApiService.searchMovies(query = query, page = page)
                            val movies = response.results.map { dto ->
                                val existing = movieDao.getMovieById(dto.id)
                                dto.toEntity().copy(isBookmarked = existing?.isBookmarked ?: false)
                            }
                            // Save as orphaned movies (no category cross-ref)
                            movieDao.insertMovies(movies)
                            LoadResult.Page(
                                data = movies,
                                prevKey = if (page == 1) null else page - 1,
                                nextKey = if (page < response.total_pages) page + 1 else null
                            )
                        } catch (e: Exception) {
                            LoadResult.Error(e)
                        }
                    }
                    override fun getRefreshKey(state: PagingState<Int, MovieEntity>): Int? = 1
                }
            }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override fun searchMoviesByTitle(query: String): Flow<List<Movie>> =
        movieDao.searchMoviesByTitle(query).map { list -> list.map { it.toDomain() } }
} 