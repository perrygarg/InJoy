package com.perrygarg.injoyapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.perrygarg.injoyapp.domain.model.Movie
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight

@Composable
fun HomeScreen(viewModel: HomeViewModel, contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val trendingMovies by viewModel.trendingMovies.collectAsStateWithLifecycle()
    val nowPlayingMovies by viewModel.nowPlayingMovies.collectAsStateWithLifecycle()

    val onBookmarkClick: (Movie) -> Unit = { /* TODO: Implement bookmark logic */ }

    // Complimentary blue-gray gradient background
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f),
            MaterialTheme.colorScheme.background
        ),
        startY = 0f,
        endY = 1200f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(contentPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp) // Reduced spacing
        ) {
            SectionHeader(title = "Trending Movies")
            MovieSection(movies = trendingMovies, onBookmarkClick = onBookmarkClick)
            SectionHeader(title = "Now Playing Movies")
            MovieSection(movies = nowPlayingMovies, onBookmarkClick = onBookmarkClick)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(28.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(3.dp)
                )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            modifier = Modifier
        )
    }
}

@Composable
fun MovieSection(movies: List<Movie>, onBookmarkClick: (Movie) -> Unit) {
    if (movies.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp), // Reduced spacing
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Reduced spacing
        ) {
            items(movies) { movie ->
                MovieCard(movie = movie, onBookmarkClick = onBookmarkClick)
            }
        }
    }
}

@Composable
fun MovieCard(
    movie: Movie,
    onBookmarkClick: (Movie) -> Unit
) {
    // Subtle vertical gradient for card background
    val cardGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        ),
        startY = 0f,
        endY = 400f
    )
    Card(
        modifier = Modifier
            .width(160.dp)
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardGradient)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val posterUrl = if (movie.posterPath.isNotBlank())
                "https://image.tmdb.org/t/p/w500${movie.posterPath}"
            else null
            Box(
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f))
            ) {
                if (posterUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(posterUrl),
                        contentDescription = movie.title,
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Image", textAlign = TextAlign.Center)
                    }
                }
                IconButton(
                    onClick = { onBookmarkClick(movie) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    if (movie.isBookmarked) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Bookmarked",
                            tint = Color.Red
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.FavoriteBorder,
                            contentDescription = "Bookmark",
                            tint = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "★",
                    color = MaterialTheme.colorScheme.tertiary,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(end = 2.dp)
                )
                Text(
                    text = String.format("%.1f", movie.voteAverage),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
} 