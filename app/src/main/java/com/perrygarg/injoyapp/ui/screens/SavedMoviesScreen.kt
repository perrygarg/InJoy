package com.perrygarg.injoyapp.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.perrygarg.injoyapp.ui.components.MovieCard
import com.perrygarg.injoyapp.ui.components.SectionHeader
import com.perrygarg.injoyapp.ui.viewmodel.SavedMoviesViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedMoviesScreen(
    viewModel: SavedMoviesViewModel = koinViewModel(),
    onMovieClick: (Int) -> Unit
) {
    val movies by viewModel.bookmarkedMovies.collectAsStateWithLifecycle()

    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
            MaterialTheme.colorScheme.background.copy(alpha = 1f)
        ),
        startY = 0f,
        endY = 1000f
    )

    Box(modifier = Modifier.fillMaxSize().background(brush = gradient)) {
        if (movies.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "No Saved Movies",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "No bookmarked movies yet!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                SectionHeader(title = "Bookmarked Movies")

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(movies.size) { index ->
                        val movie = movies[index]
                        MovieCard(
                            movie = movie,
                            onBookmarkClick = { viewModel.toggleBookmark(movie) },
                            onClick = { onMovieClick(movie.id) }
                        )
                    }
                }
            }
        }
    }
} 