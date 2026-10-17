package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.MemoryEntity
import com.example.ui.theme.LoveBackground
import com.example.ui.theme.LoveCardBorder
import com.example.ui.theme.LovePrimary
import com.example.ui.theme.LoveSecondary
import com.example.ui.theme.LoveTextMuted
import com.example.ui.theme.LoveTextPrimary
import com.example.ui.theme.LoveTextSecondary
import com.example.ui.viewmodel.LoveMapViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun AllMemoriesScreen(
    viewModel: LoveMapViewModel,
    onBack: () -> Unit,
    onSelectMemory: (MemoryEntity) -> Unit
) {
    val memories by viewModel.memories.collectAsState()
    val profile by viewModel.coupleProfile.collectAsState()

    val startDate = remember(profile) {
        profile?.startDateEpochDay?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.of(2025, 1, 1)
    }

    val filterTabs = listOf("Tất cả", "Địa điểm", "Ảnh", "Video")
    var selectedTab by remember { mutableStateOf("Tất cả") }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchInput by remember { mutableStateOf(false) }

    val filteredMemories = remember(memories, searchQuery) {
        if (searchQuery.isBlank()) {
            memories
        } else {
            memories.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.locationName.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Group memories by year
    val groupedByYear = remember(filteredMemories) {
        filteredMemories.groupBy {
            LocalDate.ofEpochDay(it.dateEpochDay).year.toString()
        }.toSortedMap(Comparator.reverseOrder())
    }

    // Group memories by location
    val groupedByLocation = remember(filteredMemories) {
        filteredMemories.groupBy {
            it.locationName.split(",").firstOrNull()?.trim() ?: it.locationName
        }
    }

    // Extract all photos across all memories
    val allPhotosWithMemory = remember(filteredMemories) {
        filteredMemories.flatMap { mem ->
            val list = mem.imagesJson.split(",").filter { it.isNotBlank() }
            val photos = if (list.isNotEmpty()) list else listOf(mem.coverImageUri)
            photos.map { photoKey -> Pair(photoKey, mem) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LoveBackground)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = LoveTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tất cả kỷ niệm",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = LoveTextPrimary
                )
            }

            IconButton(onClick = { showSearchInput = !showSearchInput }) {
                Icon(
                    imageVector = if (showSearchInput) Icons.Filled.Close else Icons.Filled.Search,
                    contentDescription = "Tìm kiếm",
                    tint = if (showSearchInput) LovePrimary else LoveTextSecondary
                )
            }
        }

        // Search Input field if toggled
        AnimatedVisibility(visible = showSearchInput) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm theo tên địa điểm, kỷ niệm...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LovePrimary,
                    unfocusedBorderColor = LoveCardBorder,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = LovePrimary)
                }
            )
        }

        // Filter Tabs (Screen 10 of mockup)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterTabs.forEach { tab ->
                val isSelected = tab == selectedTab
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    label = { Text(tab, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LovePrimary,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = LoveTextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) LovePrimary else LoveCardBorder
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content based on selected tab
        when (selectedTab) {
            "Ảnh" -> {
                // Photo Gallery Grid of all memories
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp)
                ) {
                    items(allPhotosWithMemory) { (photoKey, memory) ->
                        Box(
                            modifier = Modifier
                                .height(110.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectMemory(memory) }
                        ) {
                            if (photoKey.startsWith("content://") || photoKey.startsWith("file://")) {
                                AsyncImage(
                                    model = photoKey,
                                    contentDescription = memory.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                val res = when (photoKey) {
                                    "avatar_anh" -> R.drawable.avatar_anh
                                    "avatar_em" -> R.drawable.avatar_em
                                    else -> R.drawable.hero_couple_sunset
                                }
                                Image(
                                    painter = painterResource(id = res),
                                    contentDescription = memory.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            "Địa điểm" -> {
                // Grouped by Location
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedByLocation.forEach { (loc, locMemories) ->
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = LovePrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = loc,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LoveTextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${locMemories.size})",
                                    fontSize = 13.sp,
                                    color = LoveTextSecondary
                                )
                            }
                        }

                        items(locMemories) { memory ->
                            MemoryCardItem(
                                memory = memory,
                                startDate = startDate,
                                onClick = { onSelectMemory(memory) }
                            )
                        }
                    }
                }
            }

            else -> {
                // "Tất cả" & "Video" -> List grouped by year (Screen 10 of mockup)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedByYear.forEach { (year, yearMemories) ->
                        item {
                            Text(
                                text = "• $year •",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LoveTextMuted,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(yearMemories) { memory ->
                            MemoryCardItem(
                                memory = memory,
                                startDate = startDate,
                                onClick = { onSelectMemory(memory) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryCardItem(
    memory: MemoryEntity,
    startDate: LocalDate,
    onClick: () -> Unit
) {
    val memDate = LocalDate.ofEpochDay(memory.dateEpochDay)
    val dayNum = (ChronoUnit.DAYS.between(startDate, memDate) + 1).coerceAtLeast(1)
    val dateFormatted = memDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("memory_item_${memory.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                if (memory.coverImageUri.startsWith("content://") || memory.coverImageUri.startsWith("file://")) {
                    AsyncImage(
                        model = memory.coverImageUri,
                        contentDescription = memory.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    val res = when (memory.coverImageUri) {
                        "avatar_anh" -> R.drawable.avatar_anh
                        "avatar_em" -> R.drawable.avatar_em
                        else -> R.drawable.hero_couple_sunset
                    }
                    Image(
                        painter = painterResource(id = res),
                        contentDescription = memory.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$dateFormatted • Ngày thứ $dayNum",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = LovePrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = memory.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = LoveTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = memory.locationName,
                    fontSize = 12.sp,
                    color = LoveTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        tint = LoveTextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${memory.imagesJson.split(",").size + 2} ảnh",
                        fontSize = 11.sp,
                        color = LoveTextMuted
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = LoveCardBorder,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
