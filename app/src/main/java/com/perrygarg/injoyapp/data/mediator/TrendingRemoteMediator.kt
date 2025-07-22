package com.perrygarg.injoyapp.data.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.perrygarg.injoyapp.data.MovieApiService
import com.perrygarg.injoyapp.data.MovieDao
import com.perrygarg.injoyapp.data.MovieEntity
import com.perrygarg.injoyapp.data.toEntity
import retrofit2.HttpException
import java.io.IOException
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPagingApi::class)
class TrendingRemoteMediator(
    private val movieDao: MovieDao,
    private val movieApiService: MovieApiService
) : RemoteMediator<Int, MovieEntity>() {
    private var lastLoadedPage = 1

    private fun now(): String = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MovieEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> lastLoadedPage + 1
        }
        Log.d("TrendingRemoteMediator", "[${now()}] START load: loadType=$loadType, page=$page")
        return try {
            Log.d("TrendingRemoteMediator", "[${now()}] API CALL: getTrendingMovies(page=$page, loadType=$loadType)")
            val response = movieApiService.getTrendingMovies(page = page)
            lastLoadedPage = page
            Log.d("TrendingRemoteMediator", "[${now()}] API RESPONSE page=$page, titles=${response.results.take(3).map { it.title }}")
            val ids = response.results.map { it.id }
            val bookmarks = movieDao.getBookmarksForIds(ids).associateBy { it.id }
            val movies = response.results.map { dto ->
                val isBookmarked = bookmarks[dto.id]?.isBookmarked ?: false
                dto.toEntity("TRENDING").copy(isBookmarked = isBookmarked)
            }
            Log.d("TrendingRemoteMediator", "[${now()}] BEFORE DB INSERT page=$page, count=${movies.size}")
            if (loadType == LoadType.REFRESH) {
                movieDao.clearMoviesByCategory("TRENDING")
            }
            movieDao.insertMovies(movies)
            val count = movieDao.countByCategory("TRENDING")
            Log.d("TrendingRemoteMediator", "[${now()}] AFTER DB INSERT, DB now has $count items for TRENDING after page $page")
            Log.d("TrendingRemoteMediator", "[${now()}] RETURN MediatorResult.Success for page=$page")
            MediatorResult.Success(endOfPaginationReached = page >= response.total_pages)
        } catch (e: IOException) {
            val localCount = movieDao.countByCategory("TRENDING")
            return if (localCount > 0) {
                Log.w("TrendingRemoteMediator", "[${now()}] Network error, but Room has $localCount items. Showing stale data.")
                MediatorResult.Success(endOfPaginationReached = false)
            } else {
                Log.e("TrendingRemoteMediator", "[${now()}] Network error, no local data.", e)
                MediatorResult.Error(e)
            }
        } catch (e: HttpException) {
            val localCount = movieDao.countByCategory("TRENDING")
            return if (localCount > 0) {
                Log.w("TrendingRemoteMediator", "[${now()}] HTTP error, but Room has $localCount items. Showing stale data.")
                MediatorResult.Success(endOfPaginationReached = false)
            } else {
                Log.e("TrendingRemoteMediator", "[${now()}] HTTP error, no local data.", e)
                MediatorResult.Error(e)
            }
        }
    }
}