package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun TimelineScreen(
    viewModel: LoveMapViewModel,
    onSelectMemory: (MemoryEntity) -> Unit,
    onNavigateToMap: (lat: Double, lng: Double) -> Unit
) {
    val memories by viewModel.memories.collectAsState()
    val profile by viewModel.coupleProfile.collectAsState()

    val startDate = remember(profile) {
        profile?.startDateEpochDay?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.of(2025, 1, 1)
    }

    var selectedYearFilter by remember { mutableStateOf("Tất cả") }

    // Sorted chronological from start to present
    val sortedMemories = remember(memories, selectedYearFilter) {
        val sorted = memories.sortedBy { it.dateEpochDay }
        if (selectedYearFilter == "Tất cả") {
            sorted
        } else {
            sorted.filter { LocalDate.ofEpochDay(it.dateEpochDay).year.toString() == selectedYearFilter }
        }
    }

    val availableYears = remember(memories) {
        val set = memories.map { LocalDate.ofEpochDay(it.dateEpochDay).year.toString() }.toSortedSet(Comparator.reverseOrder())
        listOf("Tất cả") + set
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LoveBackground)
            .statusBarsPadding()
    ) {
        // Header (Matching Screen 7 of mockup)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hành trình của chúng mình",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LoveTextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = null,
                tint = LovePrimary,
                modifier = Modifier.size(18.dp)
            )
        }

        // Year Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableYears) { yr ->
                val isSelected = yr == selectedYearFilter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedYearFilter = yr },
                    label = { Text(yr, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
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

        // Vertical Timeline List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp)
        ) {
            // 1. Initial Start of Love Milestone
            item {
                TimelineMilestoneItem(
                    dateText = startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    dayLabel = "Ngày thứ 1",
                    title = "Ngày bắt đầu yêu nhau",
                    subtitle = "Cảm ơn vì đã xuất hiện trong cuộc đời nhau ❤️",
                    isHeartDot = true
                )
            }

            // 2. Memory items
            items(sortedMemories) { memory ->
                val memDate = LocalDate.ofEpochDay(memory.dateEpochDay)
                val dayNum = (ChronoUnit.DAYS.between(startDate, memDate) + 1).coerceAtLeast(1)
                val dateStr = memDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

                TimelineMemoryItem(
                    memory = memory,
                    dateText = dateStr,
                    dayLabel = "Ngày thứ $dayNum",
                    onClick = { onSelectMemory(memory) },
                    onMapClick = { onNavigateToMap(memory.latitude, memory.longitude) }
                )
            }

            // 3. Today continuing journey item
            item {
                TimelineMilestoneItem(
                    dateText = "Hôm nay",
                    dayLabel = "Tương lai",
                    title = "Hành trình của chúng mình vẫn tiếp tục",
                    subtitle = "Cùng nhau đi qua thêm thật nhiều vùng đất mới 💕",
                    isHeartDot = false
                )
            }
        }
    }
}

@Composable
private fun TimelineMilestoneItem(
    dateText: String,
    dayLabel: String,
    title: String,
    subtitle: String,
    isHeartDot: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Vertical line with dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(LovePrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(60.dp)
                    .background(LovePrimary.copy(alpha = 0.35f))
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "$dateText • $dayLabel",
                    fontSize = 12.sp,
                    color = LovePrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = LoveTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = LoveTextSecondary
                )
            }
        }
    }
}

@Composable
private fun TimelineMemoryItem(
    memory: MemoryEntity,
    dateText: String,
    dayLabel: String,
    onClick: () -> Unit,
    onMapClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Vertical line with dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(LovePrimary)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(180.dp)
                    .background(LovePrimary.copy(alpha = 0.3f))
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Memory Card (Screen 7 of mockup)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .testTag("timeline_card_${memory.id}"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "$dateText • $dayLabel",
                    fontSize = 12.sp,
                    color = LovePrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = memory.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = LoveTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Cover Image preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
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

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = LovePrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = memory.locationName,
                            fontSize = 12.sp,
                            color = LoveTextSecondary
                        )
                    }

                    // Map pin click shortcut
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onMapClick)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        color = LoveSecondary
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Map,
                                contentDescription = "Xem trên bản đồ",
                                tint = LovePrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Bản đồ",
                                fontSize = 11.sp,
                                color = LovePrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = memory.description,
                    fontSize = 13.sp,
                    color = LoveTextPrimary,
                    maxLines = 2
                )
            }
        }
    }
}
