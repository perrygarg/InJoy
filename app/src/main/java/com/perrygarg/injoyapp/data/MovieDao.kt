package com.perrygarg.injoyapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingSource

data class MovieIdBookmark(val id: Int, val isBookmarked: Boolean)

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Query("SELECT * FROM movies WHERE category = :category")
    fun getMoviesByCategory(category: String): Flow<List<MovieEntity>>

    @Update
    suspend fun updateMovie(movie: MovieEntity)

    @Query("SELECT * FROM movies WHERE id = :id LIMIT 1")
    suspend fun getMovieById(id: Int): MovieEntity?

    @Query("SELECT * FROM movies WHERE category = :category ORDER BY popularity DESC")
    fun pagingSourceByCategory(category: String): PagingSource<Int, MovieEntity>

    @Query("SELECT id, isBookmarked FROM movies WHERE id IN (:ids)")
    suspend fun getBookmarksForIds(ids: List<Int>): List<MovieIdBookmark>

    @Query("DELETE FROM movies WHERE category = :category")
    suspend fun clearMoviesByCategory(category: String)

    @Query("SELECT COUNT(*) FROM movies WHERE category = :category")
    suspend fun countByCategory(category: String): Int
} 