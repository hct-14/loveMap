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
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.LoveBackground
import com.example.ui.theme.LoveCardBorder
import com.example.ui.theme.LoveGold
import com.example.ui.theme.LovePrimary
import com.example.ui.theme.LoveSecondary
import com.example.ui.theme.LoveSecondaryContainer
import com.example.ui.theme.LoveTextMuted
import com.example.ui.theme.LoveTextPrimary
import com.example.ui.theme.LoveTextSecondary
import com.example.ui.viewmodel.LoveMapViewModel
import java.time.format.DateTimeFormatter

@Composable
fun LoveCounterScreen(
    viewModel: LoveMapViewModel,
    onBack: () -> Unit
) {
    val profile by viewModel.coupleProfile.collectAsState()
    val counterData by viewModel.counterData.collectAsState()

    val scrollState = rememberScrollState()

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
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("counter_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = LoveTextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ngày yêu nhau",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = LoveTextPrimary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Avatars with Heart
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Partner 1
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, Color.White, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.avatar_anh),
                    contentDescription = profile?.partner1Name ?: "Anh",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Heart
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .size(38.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(LovePrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Partner 2
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, Color.White, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.avatar_em),
                    contentDescription = profile?.partner2Name ?: "Em",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Day Count
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Chúng mình đã bên nhau",
                fontSize = 15.sp,
                color = LoveTextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${counterData.totalDays} ngày",
                fontSize = 46.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LovePrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (counterData.years > 0) "${counterData.years} năm yêu thương" else "${counterData.months} tháng yêu thương",
                    fontSize = 14.sp,
                    color = LoveTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = LovePrimary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4 Breakdown Cards (12 Tháng, 52 Tuần, 365 Ngày, 8.760 Giờ)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BreakdownCard(
                value = "${counterData.months}",
                label = "Tháng",
                modifier = Modifier.weight(1f)
            )
            BreakdownCard(
                value = "${counterData.weeks}",
                label = "Tuần",
                modifier = Modifier.weight(1f)
            )
            BreakdownCard(
                value = "${counterData.days}",
                label = "Ngày",
                modifier = Modifier.weight(1f)
            )
            BreakdownCard(
                value = String.format("%,d", counterData.hours),
                label = "Giờ",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Next Milestone Banner Card (Screen 9 of mockup)
        counterData.nextMilestone?.let { next ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = LoveSecondaryContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, LovePrimary.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(LovePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CardGiftcard,
                            contentDescription = null,
                            tint = LovePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Còn ${next.daysRemaining} ngày nữa đến",
                            fontSize = 12.sp,
                            color = LoveTextSecondary
                        )
                        Text(
                            text = next.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = LovePrimary
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = LovePrimary,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Milestones List (Các mốc kỷ niệm)
        Text(
            text = "Các mốc kỷ niệm",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = LoveTextPrimary,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                counterData.milestones.take(6).forEach { milestone ->
                    val dateStr = milestone.targetDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (milestone.isPassed) Color(0xFFE8F5E9) else LoveSecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (milestone.isPassed) Icons.Filled.CheckCircle else Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = if (milestone.isPassed) Color(0xFF4CAF50) else LovePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = milestone.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = LoveTextPrimary
                            )
                            Text(
                                text = dateStr,
                                fontSize = 12.sp,
                                color = LoveTextSecondary
                            )
                        }

                        // Status Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                milestone.isPassed -> Color(0xFFE8F5E9)
                                milestone.isNext -> LovePrimary
                                else -> LoveSecondary
                            }
                        ) {
                            Text(
                                text = when {
                                    milestone.isPassed -> "Đã qua"
                                    milestone.isNext -> "Đang đến"
                                    else -> "Sắp tới"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = when {
                                    milestone.isPassed -> Color(0xFF2E7D32)
                                    milestone.isNext -> Color.White
                                    else -> LoveTextSecondary
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = LoveTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = LoveTextSecondary
            )
        }
    }
}
