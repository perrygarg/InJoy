package com.perrygarg.injoyapp.di

import android.app.Application
import androidx.room.Room
import com.perrygarg.injoyapp.data.AppDatabase
import com.perrygarg.injoyapp.data.MovieApiService
import com.perrygarg.injoyapp.data.repository.MovieRepositoryImpl
import com.perrygarg.injoyapp.domain.GetNowPlayingMoviesUseCase
import com.perrygarg.injoyapp.domain.GetTrendingMoviesUseCase
import com.perrygarg.injoyapp.domain.repository.MovieRepository
import com.perrygarg.injoyapp.ui.HomeViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import com.perrygarg.injoyapp.BuildConfig

val appModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            "movies.db"
        ).build()
    }
    single { get<AppDatabase>().movieDao() }
    single {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = BuildConfig.TMDB_BEARER_TOKEN
                val headerValue = "Bearer $token"
                android.util.Log.d("KoinOkHttp", "Authorization header: $headerValue")
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", headerValue)
                    .build()
                chain.proceed(request)
            }
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single { get<Retrofit>().create(MovieApiService::class.java) }
    single<MovieRepository> { MovieRepositoryImpl(get(), get()) }
    factory { GetTrendingMoviesUseCase(get()) }
    factory { GetNowPlayingMoviesUseCase(get()) }
    viewModel {
        HomeViewModel(
            getTrendingMoviesUseCase = get(),
            getNowPlayingMoviesUseCase = get(),
            getMoviesByCategory = { category -> get<MovieRepository>().getMoviesByCategory(category) }
        )
    }
} 