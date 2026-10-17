package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AppScreen
import com.example.ui.theme.LoveBackground
import com.example.ui.theme.LoveCardBorder
import com.example.ui.theme.LovePrimary
import com.example.ui.theme.LoveSecondary
import com.example.ui.theme.LoveSecondaryContainer
import com.example.ui.theme.LoveTextMuted
import com.example.ui.theme.LoveTextPrimary
import com.example.ui.theme.LoveTextSecondary
import com.example.ui.viewmodel.LoveMapViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddMemoryScreen(
    viewModel: LoveMapViewModel,
    initialLat: Double?,
    initialLng: Double?,
    initialPlaceName: String?,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    onPickOnMap: () -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(initialPlaceName?.takeIf { it != "Điểm chọn trên bản đồ" && it != "Vị trí đã chọn" } ?: "") }
    var locationName by remember { mutableStateOf(if (title.isNotBlank()) "$title, Việt Nam" else "Việt Nam") }
    var description by remember { mutableStateOf("") }

    // Coordinates default to Hanoi Ho Guom or picked location
    var lat by remember { mutableDoubleStateOf(initialLat ?: 21.0285) }
    var lng by remember { mutableDoubleStateOf(initialLng ?: 105.8542) }

    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    var radiusMeters by remember { mutableIntStateOf(300) }

    val availableImages = listOf(
        "hero_couple_sunset",
        "avatar_anh",
        "avatar_em"
    )
    val chosenImages = remember { mutableStateListOf("hero_couple_sunset") }
    var coverImage by remember { mutableStateOf("hero_couple_sunset") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                val uriStr = uri.toString()
                if (!chosenImages.contains(uriStr)) {
                    chosenImages.add(uriStr)
                }
            }
            coverImage = uris.first().toString()
            Toast.makeText(context, "Đã thêm ${uris.size} ảnh từ thiết bị!", Toast.LENGTH_SHORT).show()
        }
    }

    val datePickerDialog = remember(selectedDate) {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
    }

    val moods = listOf(
        "Lãng mạn 💕",
        "Hạnh phúc 🥰",
        "Bình yên ☕",
        "Vui vẻ 😊",
        "Phiêu lưu ✈️"
    )
    var selectedMood by remember { mutableStateOf(moods.first()) }

    var isSaving by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LoveBackground)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(bottom = 100.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("add_memory_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = LoveTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Thêm kỷ niệm mới",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = LoveTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Image Selection Section (Matching Screen 5 of mockup)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Ảnh và video",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = LoveTextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Main Cover Preview
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (coverImage.startsWith("content://") || coverImage.startsWith("file://")) {
                        AsyncImage(
                            model = coverImage,
                            contentDescription = "Ảnh bìa",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val coverRes = when (coverImage) {
                            "avatar_anh" -> R.drawable.avatar_anh
                            "avatar_em" -> R.drawable.avatar_em
                            else -> R.drawable.hero_couple_sunset
                        }
                        Image(
                            painter = painterResource(id = coverRes),
                            contentDescription = "Ảnh bìa",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "Ảnh bìa check-in",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Photo Thumbnails Row + Add Photo Button
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(chosenImages) { imgKey ->
                    val isCover = imgKey == coverImage
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isCover) 2.5.dp else 1.dp,
                                color = if (isCover) LovePrimary else LoveCardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { coverImage = imgKey }
                    ) {
                        if (imgKey.startsWith("content://") || imgKey.startsWith("file://")) {
                            AsyncImage(
                                model = imgKey,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            val imgRes = when (imgKey) {
                                "avatar_anh" -> R.drawable.avatar_anh
                                "avatar_em" -> R.drawable.avatar_em
                                else -> R.drawable.hero_couple_sunset
                            }
                            Image(
                                painter = painterResource(id = imgRes),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                item {
                    // Add more photos button (Launches Photo Picker or cycles preset photos)
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LoveSecondary)
                            .border(1.dp, LovePrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable {
                                try {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                } catch (e: Exception) {
                                    val nextImg = availableImages.firstOrNull { !chosenImages.contains(it) }
                                        ?: availableImages.random()
                                    chosenImages.add(nextImg)
                                    Toast.makeText(context, "Đã thêm ảnh vào kỷ niệm!", Toast.LENGTH_SHORT).show()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Thêm ảnh",
                            tint = LovePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Form Fields
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Place Name
            Column {
                Text(
                    text = "Tên địa điểm *",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = LoveTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (locationName == "Việt Nam" || locationName.isBlank()) {
                            locationName = "$it, Việt Nam"
                        }
                    },
                    placeholder = { Text("Ví dụ: Hồ Gươm, Đà Lạt, Biển Mỹ Khê...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_memory_title_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LovePrimary,
                        unfocusedBorderColor = LoveCardBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
            }

            // Location coordinates & map selector
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vị trí & Tọa độ *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = LoveTextPrimary
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPickOnMap() }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = null,
                            tint = LovePrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Chọn trên bản đồ",
                            color = LovePrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = LovePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = locationName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = LoveTextPrimary
                            )
                            Text(
                                text = "Lat: ${String.format("%.4f", lat)} • Lng: ${String.format("%.4f", lng)}",
                                fontSize = 11.sp,
                                color = LoveTextMuted
                            )
                        }
                    }
                }
            }

            // Date
            Column {
                Text(
                    text = "Ngày kỷ niệm *",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = LoveTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            tint = LovePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = LoveTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Hôm nay",
                            fontSize = 12.sp,
                            color = LovePrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Story / Description
            Column {
                Text(
                    text = "Mô tả câu chuyện",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = LoveTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Khoảnh khắc đáng nhớ của hai đứa mình tại đây...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("add_memory_desc_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LovePrimary,
                        unfocusedBorderColor = LoveCardBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    maxLines = 4
                )
            }

            // Mood selector
            Column {
                Text(
                    text = "Cảm xúc",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = LoveTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(moods) { mood ->
                        val isSelected = mood == selectedMood
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMood = mood },
                            label = { Text(mood, fontSize = 12.sp) },
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
            }

            // Fog Reveal Radius
            Column {
                Text(
                    text = "Bán kính mở sáng Fog of War",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = LoveTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(100, 300, 500, 1000).forEach { radius ->
                        val isSelected = radius == radiusMeters
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) LovePrimary else Color.White)
                                .border(1.dp, if (isSelected) LovePrimary else LoveCardBorder, RoundedCornerShape(10.dp))
                                .clickable { radiusMeters = radius }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${radius}m",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else LoveTextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Save Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Vui lòng nhập tên địa điểm!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isSaving = true
                    viewModel.addMemory(
                        title = title,
                        locationName = locationName,
                        dateEpochDay = selectedDate.toEpochDay(),
                        latitude = lat,
                        longitude = lng,
                        revealRadiusMeters = radiusMeters,
                        description = description.ifBlank { "Một kỷ niệm thật đẹp bên nhau." },
                        coverImageUri = coverImage,
                        imagesJson = chosenImages.joinToString(","),
                        mood = selectedMood
                    ) { newId ->
                        isSaving = false
                        Toast.makeText(context, "Đã thắp sáng vùng đất kỷ niệm! ✨", Toast.LENGTH_SHORT).show()
                        onSaved(newId)
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = LovePrimary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_memory_button")
            ) {
                Text(
                    text = if (isSaving) "Đang thắp sáng..." else "Lưu kỷ niệm & Mở sáng bản đồ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }
    }
}
