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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
fun SettingsScreen(
    viewModel: LoveMapViewModel,
    onBack: () -> Unit,
    onNavigateToWelcome: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val profile by viewModel.coupleProfile.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showRadiusDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPairCodeDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val currentStartLocalDate = remember(profile) {
        val epoch = profile?.startDateEpochDay ?: 20089L
        LocalDate.ofEpochDay(epoch)
    }

    val startDatePicker = remember(currentStartLocalDate, profile) {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newStart = LocalDate.of(year, month + 1, dayOfMonth).toEpochDay()
                profile?.let { p ->
                    viewModel.updateCoupleProfile(
                        partner1Name = p.partner1Name,
                        partner2Name = p.partner2Name,
                        startDateEpochDay = newStart,
                        fogRadiusMeters = p.fogRadiusMeters,
                        notificationsEnabled = p.notificationsEnabled,
                        themeColorName = p.themeColorName
                    )
                    Toast.makeText(context, "Đã cập nhật ngày bắt đầu yêu!", Toast.LENGTH_SHORT).show()
                }
            },
            currentStartLocalDate.year,
            currentStartLocalDate.monthValue - 1,
            currentStartLocalDate.dayOfMonth
        )
    }

    val startDateFormatted = remember(profile) {
        val epoch = profile?.startDateEpochDay ?: 20089L
        LocalDate.ofEpochDay(epoch).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    // Edit Profile Dialog
    if (showEditProfileDialog && profile != null) {
        var p1Name by remember { mutableStateOf(profile!!.partner1Name) }
        var p2Name by remember { mutableStateOf(profile!!.partner2Name) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Chỉnh sửa tên hai người", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = p1Name,
                        onValueChange = { p1Name = it },
                        label = { Text("Tên người thứ nhất") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = p2Name,
                        onValueChange = { p2Name = it },
                        label = { Text("Tên người thứ hai") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateCoupleProfile(
                            partner1Name = p1Name.ifBlank { "Anh" },
                            partner2Name = p2Name.ifBlank { "Em" },
                            startDateEpochDay = profile!!.startDateEpochDay,
                            fogRadiusMeters = profile!!.fogRadiusMeters,
                            notificationsEnabled = profile!!.notificationsEnabled,
                            themeColorName = profile!!.themeColorName
                        )
                        showEditProfileDialog = false
                        Toast.makeText(context, "Đã cập nhật thông tin!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LovePrimary)
                ) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Hủy", color = LoveTextSecondary)
                }
            }
        )
    }

    // Fog Radius Dialog
    if (showRadiusDialog && profile != null) {
        AlertDialog(
            onDismissRequest = { showRadiusDialog = false },
            title = { Text("Bán kính mở sáng Fog of War", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(100, 300, 500, 1000).forEach { r ->
                        val isSel = profile!!.fogRadiusMeters == r
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.updateCoupleProfile(
                                        partner1Name = profile!!.partner1Name,
                                        partner2Name = profile!!.partner2Name,
                                        startDateEpochDay = profile!!.startDateEpochDay,
                                        fogRadiusMeters = r,
                                        notificationsEnabled = profile!!.notificationsEnabled,
                                        themeColorName = profile!!.themeColorName
                                    )
                                    showRadiusDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$r mét",
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) LovePrimary else LoveTextPrimary
                            )
                            if (isSel) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = null,
                                    tint = LovePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Pair Code Dialog
    if (showPairCodeDialog && profile != null) {
        AlertDialog(
            onDismissRequest = { showPairCodeDialog = false },
            title = { Text("Ghép đôi tài khoản", fontWeight = FontWeight.Bold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Mã mời riêng cho hai người:",
                        fontSize = 13.sp,
                        color = LoveTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LoveSecondaryContainer,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = profile!!.pairCode,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = LovePrimary,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gửi mã này cho nửa kia để cùng thắp sáng bản đồ kỷ niệm!",
                        fontSize = 12.sp,
                        color = LoveTextMuted
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(profile!!.pairCode))
                        Toast.makeText(context, "Đã sao chép mã mời!", Toast.LENGTH_SHORT).show()
                        showPairCodeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LovePrimary)
                ) {
                    Text("Sao chép mã")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPairCodeDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Về ứng dụng LoveMap", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "LoveMap v1.0.0",
                        fontWeight = FontWeight.Bold,
                        color = LovePrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "“Mỗi nơi chúng mình đi qua, một kỷ niệm được thắp sáng.”\n\nỨng dụng tình yêu dành cho hai người với bản đồ Fog of War, đếm ngày yêu và dòng thời gian kỷ niệm.",
                        fontSize = 13.sp,
                        color = LoveTextPrimary,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = LovePrimary)
                ) {
                    Text("Đóng")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LoveBackground)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = LoveTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Cài đặt",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = LoveTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Couple Profile Card (Matching Screen 11 of mockup)
        Text(
            text = "Thông tin của chúng mình",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = LoveTextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clickable { showEditProfileDialog = true }
                .testTag("settings_couple_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Anh
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.avatar_anh),
                            contentDescription = profile?.partner1Name ?: "Anh",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = profile?.partner1Name ?: "Anh",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = LoveTextPrimary
                    )
                }

                // Heart
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(LovePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Em
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.avatar_em),
                            contentDescription = profile?.partner2Name ?: "Em",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = profile?.partner2Name ?: "Em",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = LoveTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Settings items list (Screen 11 of mockup)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // 1. Anniversary Date
                SettingsRow(
                    icon = Icons.Filled.CalendarMonth,
                    title = "Ngày bắt đầu yêu nhau",
                    value = startDateFormatted,
                    onClick = { startDatePicker.show() }
                )

                // 2. Notification Switch
                SettingsSwitchRow(
                    icon = Icons.Filled.Notifications,
                    title = "Thông báo kỷ niệm",
                    checked = profile?.notificationsEnabled ?: true,
                    onCheckedChange = { checked ->
                        profile?.let { p ->
                            viewModel.updateCoupleProfile(
                                partner1Name = p.partner1Name,
                                partner2Name = p.partner2Name,
                                startDateEpochDay = p.startDateEpochDay,
                                fogRadiusMeters = p.fogRadiusMeters,
                                notificationsEnabled = checked,
                                themeColorName = p.themeColorName
                            )
                        }
                    }
                )

                // 3. Fog of war radius
                SettingsRow(
                    icon = Icons.Filled.Place,
                    title = "Bán kính mở sáng bản đồ",
                    value = "${profile?.fogRadiusMeters ?: 300} mét",
                    onClick = { showRadiusDialog = true }
                )

                // 4. Theme
                SettingsRow(
                    icon = Icons.Filled.Palette,
                    title = "Chủ đề giao diện",
                    value = "Hồng phấn",
                    colorBadge = LovePrimary,
                    onClick = {}
                )

                // 5. Pair Code
                SettingsRow(
                    icon = Icons.Filled.QrCode,
                    title = "Ghép đôi tài khoản",
                    value = profile?.pairCode ?: "LM-8899",
                    onClick = { showPairCodeDialog = true }
                )

                // 6. Cloud & Security
                SettingsRow(
                    icon = Icons.Filled.Lock,
                    title = "Bảo mật & đồng bộ",
                    value = "Đã bật",
                    onClick = {
                        Toast.makeText(context, "Dữ liệu được lưu trữ bảo mật cục bộ & sẵn sàng đồng bộ đám mây", Toast.LENGTH_SHORT).show()
                    }
                )

                // 7. Language
                SettingsRow(
                    icon = Icons.Filled.Language,
                    title = "Ngôn ngữ",
                    value = "Tiếng Việt",
                    onClick = {}
                )

                // 8. About App
                SettingsRow(
                    icon = Icons.Filled.Info,
                    title = "Giới thiệu ứng dụng",
                    value = "v1.0.0",
                    isLast = false,
                    onClick = { showAboutDialog = true }
                )

                // 9. Revisit Welcome / Onboarding Screen
                SettingsRow(
                    icon = Icons.Filled.Favorite,
                    title = "Xem lại màn hình chào",
                    value = "Mở",
                    isLast = true,
                    onClick = onNavigateToWelcome
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    value: String? = null,
    colorBadge: Color? = null,
    isLast: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LovePrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            color = LoveTextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        if (colorBadge != null) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(colorBadge)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        if (value != null) {
            Text(
                text = value,
                fontSize = 13.sp,
                color = LoveTextSecondary
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = LoveCardBorder,
            modifier = Modifier.size(12.dp)
        )
    }

    if (!isLast) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(0.5.dp)
                .background(LoveCardBorder.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LovePrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            color = LoveTextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = LovePrimary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = LoveCardBorder
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(0.5.dp)
            .background(LoveCardBorder.copy(alpha = 0.5f))
    )
}
