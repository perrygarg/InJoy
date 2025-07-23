package com.perrygarg.injoyapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MovieEntity::class, MovieCategoryCrossRef::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
} 