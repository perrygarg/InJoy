package com.perrygarg.injoyapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.perrygarg.injoyapp.R
import com.perrygarg.injoyapp.domain.model.Movie
import java.util.Locale

@Composable
fun MovieCard(
    movie: Movie,
    onBookmarkClick: (Movie) -> Unit,
    onClick: (() -> Unit)? = null,
    showBookmarkIcon: Boolean = true
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
            .padding(vertical = 6.dp)
            .clickable(enabled = onClick != null) { onClick?.invoke() },
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
                        painter = rememberAsyncImagePainter(
                            model = posterUrl,
                            error = painterResource(R.drawable.outline_movie_24),
                            placeholder = painterResource(R.drawable.outline_movie_24)
                        ),
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
                if (showBookmarkIcon) {
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
                    modifier = Modifier.padding(end = 2.dp, bottom = 4.dp)
                )
                Text(
                    text = String.format(Locale.US,"%.1f", movie.voteAverage),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}