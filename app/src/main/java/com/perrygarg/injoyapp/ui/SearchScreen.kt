package com.perrygarg.injoyapp.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = koinViewModel(),
    onMovieClick: (Int) -> Unit = {}
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
    val offlineResults by viewModel.offlineResults.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val isTyping = searchResults.loadState.refresh is androidx.paging.LoadState.Loading && query.isNotBlank() && !isOffline
    val coroutineScope = rememberCoroutineScope()
    val isOfflineWarning by viewModel.isOfflineWarning.collectAsStateWithLifecycle()

    // Observe network status and update isOffline
    val isOfflineState: MutableStateFlow<Boolean> = koinInject()
    LaunchedEffect(isOffline) {
        isOfflineState.value = isOffline
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isOfflineWarning) {
            OfflineWarningTooltip()
            Spacer(modifier = Modifier.height(8.dp))
        }
        SearchBar(
            value = query,
            onValueChange = viewModel::onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (isTyping) {
            ShimmerGridPlaceholder()
        } else if (query.isNotBlank() && ((isOffline && offlineResults.isEmpty()) || (!isOffline && searchResults.itemCount == 0))) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CloudOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No results found", style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else if (isOffline) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(offlineResults.size) { index ->
                    val movie = offlineResults[index]
                    MovieCard(
                        movie = movie,
                        onBookmarkClick = { viewModel.toggleBookmark(movie) },
                        onClick = { onMovieClick(movie.id) },
                        showBookmarkIcon = false
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults.itemCount) { index ->
                    val movie = searchResults[index]
                    if (movie != null) {
                        MovieCard(
                            movie = movie,
                            onBookmarkClick = { viewModel.toggleBookmark(movie) },
                            onClick = { onMovieClick(movie.id) },
                            showBookmarkIcon = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
        placeholder = { Text("Search movies...", style = MaterialTheme.typography.bodyLarge) },
        modifier = modifier
            .height(48.dp)
            .clip(MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun ShimmerGridPlaceholder() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(6) {
            ShimmerMovieCardPlaceholder()
        }
    }
} 