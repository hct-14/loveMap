package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R

/**
 * High-performance image component that seamlessly displays either:
 * 1. Bundled local drawable resources (e.g. "avatar_anh", "avatar_em", "hero_couple_sunset")
 * 2. User gallery photos from device URIs (e.g. "content://media/external/images/...")
 * with automatic caching and zero startup latency.
 */
@Composable
fun LoveImage(
    imageUri: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val uri = imageUri?.trim().orEmpty()

    val localDrawableRes = when (uri) {
        "avatar_anh" -> R.drawable.avatar_anh
        "avatar_em" -> R.drawable.avatar_em
        "hero_couple_sunset" -> R.drawable.hero_couple_sunset
        "ic_launcher_lovemap" -> R.drawable.ic_launcher_lovemap
        else -> null
    }

    if (localDrawableRes != null) {
        Image(
            painter = painterResource(id = localDrawableRes),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else if (uri.startsWith("content://") || uri.startsWith("file://") || uri.startsWith("http://") || uri.startsWith("https://")) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .crossfade(true)
                .error(R.drawable.hero_couple_sunset)
                .placeholder(R.drawable.hero_couple_sunset)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.hero_couple_sunset),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}
