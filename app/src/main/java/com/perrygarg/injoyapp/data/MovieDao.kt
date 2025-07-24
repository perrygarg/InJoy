package com.perrygarg.injoyapp.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class MovieIdBookmark(val id: Int, val isBookmarked: Boolean)

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovieCategoryCrossRefs(refs: List<MovieCategoryCrossRef>)

    @Update
    suspend fun updateMovie(movie: MovieEntity)

    @Query("SELECT * FROM movies WHERE id = :id LIMIT 1")
    suspend fun getMovieById(id: Int): MovieEntity?

    @Query("SELECT id, isBookmarked FROM movies WHERE id IN (:ids)")
    suspend fun getBookmarksForIds(ids: List<Int>): List<MovieIdBookmark>

    @Query("DELETE FROM MovieCategoryCrossRef WHERE category = :category")
    suspend fun clearCategory(category: String)

    @Query("SELECT * FROM movies WHERE isBookmarked = 1 ORDER BY popularity DESC")
    fun getBookmarkedMovies(): Flow<List<MovieEntity>>

    @Query("SELECT m.* FROM movies m INNER JOIN MovieCategoryCrossRef c ON m.id = c.movieId WHERE c.category = :category ORDER BY c.position ASC")
    fun pagingSourceByCategory(category: String): PagingSource<Int, MovieEntity>

    @Query("SELECT * FROM movies WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%' ORDER BY popularity DESC")
    fun searchMoviesByTitle(query: String): Flow<List<MovieEntity>>
} 