package com.perrygarg.injoyapp.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.placeholder.placeholder
import com.perrygarg.injoyapp.domain.model.Movie
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun observeNetworkStatus(context: Context) = callbackFlow<Boolean> {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: android.net.Network) {
            trySend(true)
        }
        override fun onLost(network: android.net.Network) {
            trySend(false)
        }
    }
    val networkRequest = android.net.NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()
    connectivityManager.registerNetworkCallback(networkRequest, callback)
    trySend(isNetworkAvailable(context)) // Initial value
    awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
}

@Composable
fun HomeScreen(viewModel: HomeViewModel, contentPadding: PaddingValues = PaddingValues(0.dp)) {
    val context = LocalContext.current
    var isOffline by remember { mutableStateOf(!isNetworkAvailable(context)) }
    LaunchedEffect(context) {
        observeNetworkStatus(context)
            .distinctUntilChanged()
            .collect { isOnline ->
                isOffline = !isOnline
            }
    }
    val trendingPagingItems = viewModel.trendingPagingData.collectAsLazyPagingItems()
    val nowPlayingState by viewModel.nowPlayingState.collectAsStateWithLifecycle()

    val onBookmarkClick: (Movie) -> Unit = { viewModel.toggleBookmark(it) }

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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SectionHeader(title = "Trending Movies")
            if (isOffline) {
                OfflineWarningTooltip()
            }
            TrendingMoviePagingSection(trendingPagingItems, onBookmarkClick)
            SectionHeader(title = "Now Playing Movies")
            MovieSection(
                state = nowPlayingState,
                onBookmarkClick = onBookmarkClick,
                onRetry = { viewModel.fetchNowPlaying() }
            )
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
fun MovieSection(
    state: SectionUiState<Movie>,
    onBookmarkClick: (Movie) -> Unit,
    onRetry: () -> Unit
) {
    when (state) {
        is SectionUiState.Loading -> {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(4) { ShimmerMovieCardPlaceholder() }
            }
        }
        is SectionUiState.Error -> {
            ErrorState(message = state.message, onRetry = onRetry)
        }
        is SectionUiState.Empty -> {
            NoDataState()
        }
        is SectionUiState.Success -> {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.data) { movie ->
                    MovieCard(movie = movie, onBookmarkClick = onBookmarkClick)
                }
            }
        }
    }
}

@Composable
fun TrendingMoviePagingSection(
    pagingItems: LazyPagingItems<Movie>,
    onBookmarkClick: (Movie) -> Unit
) {
    when (pagingItems.loadState.refresh) {
        is androidx.paging.LoadState.Loading -> {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(4) { ShimmerMovieCardPlaceholder() }
            }
        }
        is androidx.paging.LoadState.Error -> {
            ErrorState(message = "Failed to load movies", onRetry = { pagingItems.retry() })
        }
        else -> {
            if (pagingItems.itemCount == 0) {
                NoDataState()
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(count = pagingItems.itemCount) { index ->
                        val movie = pagingItems[index]
                        if (movie != null) {
                            MovieCard(movie = movie, onBookmarkClick = onBookmarkClick)
                        }
                    }
                    if (pagingItems.loadState.append is androidx.paging.LoadState.Loading) {
                        item { ShimmerMovieCardPlaceholder() }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

@Composable
fun NoDataState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = "No Data",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No data available",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun OfflineWarningTooltip() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = "Offline",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "You are offline. Data may be stale.",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ShimmerMovieCardPlaceholder() {
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
            Box(
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                        color = Color(0xFFEEEEEE),
                        shape = RoundedCornerShape(12.dp)
                    )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .height(18.dp)
                    .fillMaxWidth(0.7f)
                    .clip(RoundedCornerShape(6.dp))
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                        color = Color(0xFFEEEEEE),
                        shape = RoundedCornerShape(6.dp)
                    )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth(0.4f)
                    .clip(RoundedCornerShape(6.dp))
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                        color = Color(0xFFEEEEEE),
                        shape = RoundedCornerShape(6.dp)
                    )
            )
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