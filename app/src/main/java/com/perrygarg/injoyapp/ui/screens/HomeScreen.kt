package com.perrygarg.injoyapp.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.perrygarg.injoyapp.domain.model.Movie
import com.perrygarg.injoyapp.ui.components.MovieCard
import com.perrygarg.injoyapp.ui.components.OfflineWarningTooltip
import com.perrygarg.injoyapp.ui.components.SectionHeader
import com.perrygarg.injoyapp.ui.components.ShimmerMovieCardPlaceholder
import com.perrygarg.injoyapp.ui.viewmodel.HomeViewModel
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
        override fun onAvailable(network: Network) {
            trySend(true)
        }
        override fun onLost(network: Network) {
            trySend(false)
        }
    }
    val networkRequest = NetworkRequest.Builder()
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
    val nowPlayingPagingItems = viewModel.nowPlayingPagingData.collectAsLazyPagingItems()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()

    val onBookmarkClick: (Movie) -> Unit = { viewModel.toggleBookmark(it) }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
            MaterialTheme.colorScheme.background.copy(alpha = 1f)
        ),
        startY = 0f,
        endY = 1000f
    )

    SwipeRefresh(
        modifier = Modifier.fillMaxSize()
            .background(gradient),
        state = rememberSwipeRefreshState(isRefreshing = refreshing),
        onRefresh = {
            if (!isOffline) {
                trendingPagingItems.refresh()
                nowPlayingPagingItems.refresh()
            }
        },
        indicatorPadding = contentPadding
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
                .padding(top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (isOffline) {
                item { OfflineWarningTooltip() }
            }
            item { SectionHeader(title = "Trending Movies") }
            item {
                CommonMoviePagingSection(trendingPagingItems, onBookmarkClick) { movie ->
                    viewModel.navigateToDetail(movie)
                }
            }
            item { SectionHeader(title = "Now Playing Movies") }
            item {
                CommonMoviePagingSection(nowPlayingPagingItems, onBookmarkClick) { movie ->
                    viewModel.navigateToDetail(movie)
                }
            }
        }
    }
}

@Composable
fun CommonMoviePagingSection(
    pagingItems: LazyPagingItems<Movie>,
    onBookmarkClick: (Movie) -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    when (pagingItems.loadState.refresh) {
        is LoadState.Loading -> {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(4) { ShimmerMovieCardPlaceholder() }
            }
        }
        is LoadState.Error -> {
            if (pagingItems.itemCount == 0) {
                ErrorState(message = "Failed to load movies", onRetry = { pagingItems.retry() })
            }
        }
        else -> {
            if (pagingItems.itemCount == 0) {
                NoDataState()
            } else {
                val listState = rememberLazyListState()
                LazyRow(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(count = pagingItems.itemCount) { index ->
                        val movie = pagingItems[index]
                        if (movie != null) {
                            MovieCard(movie = movie, onBookmarkClick = onBookmarkClick, onClick = { onMovieClick(movie) }, showBookmarkIcon = false)
                        }
                    }
                    if (pagingItems.loadState.append is LoadState.Loading) {
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
            text = "No data available. Use pull to refresh.",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}