package com.perrygarg.injoyapp.di

import androidx.room.Room
import com.perrygarg.injoyapp.BuildConfig
import com.perrygarg.injoyapp.data.AppDatabase
import com.perrygarg.injoyapp.data.MovieApiService
import com.perrygarg.injoyapp.data.repository.MovieRepositoryImpl
import com.perrygarg.injoyapp.domain.GetBookmarkedMoviesUseCase
import com.perrygarg.injoyapp.domain.GetMovieDetailUseCase
import com.perrygarg.injoyapp.domain.GetNowPlayingMoviesPagerUseCase
import com.perrygarg.injoyapp.domain.GetTrendingMoviesPagerUseCase
import com.perrygarg.injoyapp.domain.SearchMoviesByTitleUseCase
import com.perrygarg.injoyapp.domain.SearchMoviesPagerUseCase
import com.perrygarg.injoyapp.domain.UpdateBookmarkUseCase
import com.perrygarg.injoyapp.domain.repository.MovieRepository
import com.perrygarg.injoyapp.ui.HomeViewModel
import com.perrygarg.injoyapp.ui.MovieDetailViewModel
import com.perrygarg.injoyapp.ui.SavedMoviesViewModel
import com.perrygarg.injoyapp.ui.SearchViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
    factory { UpdateBookmarkUseCase(get()) }
    factory { GetMovieDetailUseCase(get()) }
    factory { GetBookmarkedMoviesUseCase(get()) }
    factory { GetTrendingMoviesPagerUseCase(get()) }
    factory { GetNowPlayingMoviesPagerUseCase(get()) }
    factory { SearchMoviesPagerUseCase(get()) }
    factory { SearchMoviesByTitleUseCase(get()) }
    single {
        MutableStateFlow(false)
    } bind StateFlow::class
    viewModel {
        HomeViewModel(
            updateBookmarkUseCase = get(),
            getTrendingMoviesPagerUseCase = get(),
            getNowPlayingMoviesPagerUseCase = get()
        )
    }
    viewModel {
        MovieDetailViewModel(
            getMovieDetailUseCase = get(),
            updateBookmarkUseCase = get()
        )
    }
    viewModel {
        SavedMoviesViewModel(
            getBookmarkedMoviesUseCase = get(),
            updateBookmarkUseCase = get()
        )
    }
    viewModel {
        SearchViewModel(
            searchMoviesPagerUseCase = get(),
            searchMoviesByTitleUseCase = get(),
            updateBookmarkUseCase = get(),
            isOffline = get()
        )
    }
} 