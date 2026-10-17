package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
fun MemoryDetailScreen(
    memory: MemoryEntity,
    viewModel: LoveMapViewModel,
    onBack: () -> Unit,
    onViewOnMap: (lat: Double, lng: Double) -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.coupleProfile.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedFullPhoto by remember { mutableStateOf<Int?>(null) }

    var editTitle by remember(memory) { mutableStateOf(memory.title) }
    var editLocation by remember(memory) { mutableStateOf(memory.locationName) }
    var editDesc by remember(memory) { mutableStateOf(memory.description) }
    var editMood by remember(memory) { mutableStateOf(memory.mood) }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Chỉnh sửa kỷ niệm", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    androidx.compose.material3.OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Tên địa điểm") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editLocation,
                        onValueChange = { editLocation = it },
                        label = { Text("Khu vực") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    androidx.compose.material3.OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Mô tả / Câu chuyện") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateMemory(
                            memory.copy(
                                title = editTitle.ifBlank { memory.title },
                                locationName = editLocation.ifBlank { memory.locationName },
                                description = editDesc.ifBlank { memory.description },
                                mood = editMood
                            )
                        ) {
                            showEditDialog = false
                            Toast.makeText(context, "Đã cập nhật kỷ niệm!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LovePrimary)
                ) {
                    Text("Lưu thay đổi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Hủy", color = LoveTextSecondary)
                }
            }
        )
    }

    val startDate = remember(profile) {
        profile?.startDateEpochDay?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.of(2025, 1, 1)
    }
    val memoryDate = remember(memory) {
        LocalDate.ofEpochDay(memory.dateEpochDay)
    }

    val dayNumber = remember(startDate, memoryDate) {
        (ChronoUnit.DAYS.between(startDate, memoryDate) + 1).coerceAtLeast(1)
    }

    val dateFormatted = remember(memoryDate) {
        memoryDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    val scrollState = rememberScrollState()

    val photos = remember(memory) {
        val list = memory.imagesJson.split(",").filter { it.isNotBlank() }
        if (list.isNotEmpty()) list else listOf("hero_couple_sunset")
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa kỷ niệm này?", fontWeight = FontWeight.Bold) },
            text = { Text("Kỷ niệm và vùng thắp sáng trên bản đồ sẽ bị xóa. Bạn có chắc chắn không?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMemory(memory.id) {
                            showDeleteDialog = false
                            Toast.makeText(context, "Đã xóa kỷ niệm", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LovePrimary)
                ) {
                    Text("Xác nhận xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy", color = LoveTextSecondary)
                }
            }
        )
    }

    // Fullscreen Photo Lightbox
    if (selectedFullPhoto != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { selectedFullPhoto = null },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = selectedFullPhoto!!),
                contentDescription = "Xem ảnh lớn",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .clickable { selectedFullPhoto = null },
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "✕ Đóng",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LoveBackground)
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp)
    ) {
        // Hero Image Header (Screen 8 of mockup)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            val heroRes = when (memory.coverImageUri) {
                "avatar_anh" -> R.drawable.avatar_anh
                "avatar_em" -> R.drawable.avatar_em
                else -> R.drawable.hero_couple_sunset
            }
            Image(
                painter = painterResource(id = heroRes),
                contentDescription = memory.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { selectedFullPhoto = heroRes }
            )

            // Gradient shade
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Top action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.White
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Chỉnh sửa",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Xóa",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Memory Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Title
                Text(
                    text = memory.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LoveTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Location row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = LovePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = memory.locationName,
                        fontSize = 14.sp,
                        color = LoveTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Date & Day counter
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = LovePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$dateFormatted • Ngày thứ $dayNumber",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LovePrimary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description story
                Text(
                    text = memory.description,
                    fontSize = 14.sp,
                    color = LoveTextPrimary,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mood chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LoveSecondary
                ) {
                    Text(
                        text = memory.mood,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = LovePrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Album Photo Grid Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Album ảnh (${photos.size + 3})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = LoveTextPrimary
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = LovePrimary,
                        modifier = Modifier.size(13.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2x3 Grid of photos matching mockup
                val galleryItems = listOf(
                    R.drawable.hero_couple_sunset,
                    R.drawable.avatar_anh,
                    R.drawable.avatar_em,
                    R.drawable.hero_couple_sunset,
                    R.drawable.avatar_em,
                    R.drawable.avatar_anh
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in 0 until 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (col in 0 until 3) {
                                val index = row * 3 + col
                                val itemRes = galleryItems[index]
                                val isLast = index == 5

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedFullPhoto = itemRes }
                                ) {
                                    Image(
                                        painter = painterResource(id = itemRes),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    if (isLast) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.55f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "+3",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action button to View on Map
        Button(
            onClick = {
                onViewOnMap(memory.latitude, memory.longitude)
            },
            colors = ButtonDefaults.buttonColors(containerColor = LovePrimary),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(50.dp)
                .testTag("memory_view_on_map_btn")
        ) {
            Icon(
                imageVector = Icons.Filled.Map,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Xem vị trí trên bản đồ",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
