package com.perrygarg.injoyapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.perrygarg.injoyapp.domain.model.Movie
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.PaddingValues

@Composable
fun HomeScreen(viewModel: HomeViewModel, contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val trendingMovies by viewModel.trendingMovies.collectAsStateWithLifecycle()
    val nowPlayingMovies by viewModel.nowPlayingMovies.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(contentPadding).padding(16.dp)) {
        Text(
            text = "Trending Movies",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        MovieSection(movies = trendingMovies)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Now Playing Movies",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        MovieSection(movies = nowPlayingMovies)
    }
}

@Composable
fun MovieSection(movies: List<Movie>) {
    if (movies.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
            // Or use a shimmer effect here if available
        }
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(movies) { movie ->
                MovieCard(movie = movie)
            }
        }
    }
}

@Composable
fun MovieCard(movie: Movie) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val posterUrl = if (movie.posterPath.isNotBlank())
            "https://image.tmdb.org/t/p/w500${movie.posterPath}"
        else null
        if (posterUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(posterUrl),
                contentDescription = movie.title,
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("No Image", textAlign = TextAlign.Center)
            }
        }
        Text(
            text = movie.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 2,
            textAlign = TextAlign.Center
        )
    }
} 