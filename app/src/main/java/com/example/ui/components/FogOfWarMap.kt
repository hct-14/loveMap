package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.data.MemoryEntity
import com.example.ui.theme.LoveAccentRed
import com.example.ui.theme.LoveBackground
import com.example.ui.theme.LoveCardBorder
import com.example.ui.theme.LovePrimary
import com.example.ui.theme.LovePrimaryDark
import com.example.ui.theme.LoveSecondary
import com.example.ui.theme.LoveTextMuted
import com.example.ui.theme.LoveTextPrimary
import com.example.ui.theme.LoveTextSecondary
import com.example.ui.theme.MapDarkBg
import com.example.ui.theme.MapGlowReveal
import com.example.ui.theme.MapLand
import com.example.ui.theme.MapRevealedLand
import com.example.ui.theme.MapRevealedWater
import com.example.ui.theme.MapRoadPrimary
import com.example.ui.theme.MapRoadSecondary
import com.example.ui.theme.MapWater
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Geographic projection bounds
// Default covers Vietnam comfortably: Lat 8.0N to 24.0N, Lng 101.5E to 110.0E
private const val MIN_LAT = 8.0
private const val MAX_LAT = 24.0
private const val MIN_LNG = 101.5
private const val MAX_LNG = 110.0

@Composable
fun FogOfWarMap(
    memories: List<MemoryEntity>,
    targetCoordinate: Pair<Double, Double>?,
    selectedMemory: MemoryEntity?,
    onSelectMemory: (MemoryEntity?) -> Unit,
    onContinueToForm: (lat: Double, lng: Double, placeName: String) -> Unit,
    onViewMemoryDetail: (MemoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Map Transform state (Zoom & Pan)
    var zoom by remember { mutableFloatStateOf(1.6f) }
    var panOffset by remember { mutableStateOf(Offset(0f, -80f)) }

    // Manual dropped pin
    var droppedPin by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var droppedPlaceName by remember { mutableStateOf("Vị trí đã chọn") }

    // Search query
    var searchQuery by remember { mutableStateOf("") }

    // Radar pulse animation for revealed zones
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseFraction"
    )

    // Handle target coordinate navigation from Timeline or Details
    LaunchedEffect(targetCoordinate) {
        if (targetCoordinate != null) {
            zoom = 3.2f
            // Pan will be recentered in BoxWithConstraints once dimensions are known
        }
    }

    // GPS Location Client
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var isLocating by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            try {
                isLocating = true
                fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                    isLocating = false
                    if (loc != null) {
                        userLocation = Pair(loc.latitude, loc.longitude)
                        droppedPin = Pair(loc.latitude, loc.longitude)
                        droppedPlaceName = "Vị trí GPS hiện tại"
                        zoom = 3.5f
                    }
                }
            } catch (e: SecurityException) {
                isLocating = false
            }
        }
    }

    fun requestGps() {
        val fineCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (fineCheck == PackageManager.PERMISSION_GRANTED) {
            isLocating = true
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                    isLocating = false
                    if (loc != null) {
                        userLocation = Pair(loc.latitude, loc.longitude)
                        droppedPin = Pair(loc.latitude, loc.longitude)
                        droppedPlaceName = "Vị trí GPS của chúng mình"
                        zoom = 3.5f
                    }
                }
            } catch (e: SecurityException) {
                isLocating = false
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        zoom = (zoom * zoomChange).coerceIn(0.8f, 8.0f)
        panOffset += offsetChange
    }

    var showSearchDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MapDarkBg)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        val popularDestinations = remember {
            listOf(
                Triple("Hà Nội (Hồ Gươm)", 21.0285, 105.8542),
                Triple("Đà Nẵng (Bà Nà & Mỹ Khê)", 16.0544, 108.2022),
                Triple("Đà Lạt (Hồ Xuân Hương)", 11.9404, 108.4583),
                Triple("TP. Hồ Chí Minh (Nguyễn Huệ)", 10.7769, 106.7009),
                Triple("Hội An (Chùa Cầu & Sông Hoài)", 15.8801, 108.3380),
                Triple("Phú Quốc (Bãi Sao)", 10.2289, 103.9572),
                Triple("Sa Pa (Fansipan)", 22.3364, 103.8438),
                Triple("Nha Trang (Biển Trần Phú)", 12.2388, 109.1967)
            )
        }

        if (showSearchDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showSearchDialog = false },
                title = {
                    Text(
                        "Tìm kiếm & Khám phá địa điểm",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = LoveTextPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Chọn hoặc bay nhanh đến các thành phố kỷ niệm:",
                            fontSize = 13.sp,
                            color = LoveTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        popularDestinations.forEach { (cityName, cLat, cLng) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        val baseWidth = widthPx * 1.5f
                                        val baseHeight = heightPx * 1.5f
                                        val normX = (cLng - MIN_LNG) / (MAX_LNG - MIN_LNG)
                                        val normY = 1.0 - (cLat - MIN_LAT) / (MAX_LAT - MIN_LAT)
                                        val localX = (normX * baseWidth).toFloat()
                                        val localY = (normY * baseHeight).toFloat()
                                        zoom = 3.6f
                                        panOffset = Offset(
                                            -(localX - baseWidth / 2f) * 3.6f,
                                            -(localY - baseHeight / 2f) * 3.6f
                                        )
                                        val existing = memories.firstOrNull {
                                            kotlin.math.abs(it.latitude - cLat) < 0.1 &&
                                            kotlin.math.abs(it.longitude - cLng) < 0.1
                                        }
                                        if (existing != null) {
                                            onSelectMemory(existing)
                                            droppedPin = null
                                        } else {
                                            droppedPin = Pair(cLat, cLng)
                                            droppedPlaceName = cityName
                                            onSelectMemory(null)
                                        }
                                        showSearchDialog = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Place,
                                    contentDescription = null,
                                    tint = LovePrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = cityName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LoveTextPrimary
                                )
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showSearchDialog = false }) {
                        Text("Đóng", color = LovePrimary)
                    }
                }
            )
        }

        // Helper function to project Lat/Lng to Canvas pixels
        fun geoToPixel(lat: Double, lng: Double): Offset {
            val normX = (lng - MIN_LNG) / (MAX_LNG - MIN_LNG)
            // Latitude inverts Y
            val normY = 1.0 - (lat - MIN_LAT) / (MAX_LAT - MIN_LAT)

            val baseWidth = widthPx * 1.5f
            val baseHeight = heightPx * 1.5f

            val localX = (normX * baseWidth).toFloat()
            val localY = (normY * baseHeight).toFloat()

            // Center of the canvas
            val centerX = widthPx / 2f
            val centerY = heightPx / 2f

            val scaledX = (localX - baseWidth / 2f) * zoom + centerX + panOffset.x
            val scaledY = (localY - baseHeight / 2f) * zoom + centerY + panOffset.y

            return Offset(scaledX, scaledY)
        }

        // Helper function to convert Canvas pixel back to Lat/Lng
        fun pixelToGeo(px: Float, py: Float): Pair<Double, Double> {
            val baseWidth = widthPx * 1.5f
            val baseHeight = heightPx * 1.5f

            val centerX = widthPx / 2f
            val centerY = heightPx / 2f

            val localX = (px - centerX - panOffset.x) / zoom + baseWidth / 2f
            val localY = (py - centerY - panOffset.y) / zoom + baseHeight / 2f

            val normX = (localX / baseWidth).toDouble().coerceIn(0.0, 1.0)
            val normY = (localY / baseHeight).toDouble().coerceIn(0.0, 1.0)

            val lng = MIN_LNG + normX * (MAX_LNG - MIN_LNG)
            val lat = MAX_LAT - normY * (MAX_LAT - MIN_LAT)

            return Pair(lat, lng)
        }

        // Handle auto-panning if targetCoordinate is specified
        LaunchedEffect(targetCoordinate, widthPx, heightPx) {
            if (targetCoordinate != null) {
                val baseWidth = widthPx * 1.5f
                val baseHeight = heightPx * 1.5f
                val normX = (targetCoordinate.second - MIN_LNG) / (MAX_LNG - MIN_LNG)
                val normY = 1.0 - (targetCoordinate.first - MIN_LAT) / (MAX_LAT - MIN_LAT)
                val localX = (normX * baseWidth).toFloat()
                val localY = (normY * baseHeight).toFloat()

                panOffset = Offset(
                    -(localX - baseWidth / 2f) * zoom,
                    -(localY - baseHeight / 2f) * zoom
                )
            }
        }

        // 1. BASE MAP CANVAS (Static: Coastline, waterways, highways, romantic journey path & revealed fog discs)
        // Does NOT subscribe to pulseFraction, avoiding 60fps re-allocations and redraws
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformableState)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            val (lat, lng) = pixelToGeo(tapOffset.x, tapOffset.y)
                            // Check if tapped near an existing memory marker
                            val tappedMemory = memories.firstOrNull { mem ->
                                val pos = geoToPixel(mem.latitude, mem.longitude)
                                val distSq = (pos.x - tapOffset.x) * (pos.x - tapOffset.x) +
                                        (pos.y - tapOffset.y) * (pos.y - tapOffset.y)
                                distSq < 48 * 48
                            }

                            if (tappedMemory != null) {
                                onSelectMemory(tappedMemory)
                                droppedPin = null
                            } else {
                                onSelectMemory(null)
                                droppedPin = Pair(lat, lng)
                                droppedPlaceName = "Điểm chọn trên bản đồ"
                            }
                        }
                    )
                }
        ) {
            // Draw Map Background (Ocean & Land outlines)
            drawRect(color = MapDarkBg)

            // Draw stylized coastline & river veins
            drawCoastlineAndGeography(
                geoToPixel = ::geoToPixel,
                zoom = zoom
            )

            // Draw Street Grid / Highways
            drawRoadNetwork(
                geoToPixel = ::geoToPixel,
                zoom = zoom
            )

            // Dotted romantic journey path connecting visited check-in points
            val sortedMemories = memories.sortedBy { it.dateEpochDay }
            if (sortedMemories.size > 1) {
                for (i in 0 until sortedMemories.size - 1) {
                    val from = geoToPixel(sortedMemories[i].latitude, sortedMemories[i].longitude)
                    val to = geoToPixel(sortedMemories[i + 1].latitude, sortedMemories[i + 1].longitude)
                    val journeyPath = Path().apply {
                        moveTo(from.x, from.y)
                        val midX = (from.x + to.x) / 2f
                        val midY = (from.y + to.y) / 2f + 20f
                        quadraticTo(midX, midY, to.x, to.y)
                    }
                    drawPath(
                        path = journeyPath,
                        color = LovePrimary.copy(alpha = 0.8f),
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                        )
                    )
                }
            }

            // FOG OF WAR ENGINE: Draw revealed illuminated circles with gradient borders
            drawRevealedFogOfWarDiscs(
                memories = memories,
                geoToPixel = ::geoToPixel,
                zoom = zoom
            )
        }

        // 2. LIGHTWEIGHT ANIMATED BEACON CANVAS (Only draws expanding radar pulses)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawBeaconRings(
                memories = memories,
                geoToPixel = ::geoToPixel,
                zoom = zoom,
                pulseFraction = pulseFraction
            )
        }

        // 3. Marker Overlay Elements (Composed on top for crispness and interaction)
        memories.forEach { memory ->
            val pos = geoToPixel(memory.latitude, memory.longitude)
            // Only render if in viewport bounds
            if (pos.x in -100f..(widthPx + 100f) && pos.y in -100f..(heightPx + 100f)) {
                PhotoPinMarker(
                    memory = memory,
                    isSelected = selectedMemory?.id == memory.id,
                    offset = pos,
                    onClick = {
                        onSelectMemory(memory)
                        droppedPin = null
                    }
                )
            }
        }

        // 5. Dropped Candidate Pin (when user taps to add)
        droppedPin?.let { pin ->
            val pos = geoToPixel(pin.first, pin.second)
            CandidatePinMarker(
                offset = pos,
                pulseFraction = pulseFraction
            )
        }

        // 6. Top Search / Status Bar
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .clickable { showSearchDialog = true },
            shape = RoundedCornerShape(24.dp),
            color = Color(0xF0101722),
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, LovePrimary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Tìm kiếm",
                    tint = LovePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                val mapCoverage = (memories.size * 6.5f).coerceAtMost(100f)
                Text(
                    text = if (searchQuery.isEmpty()) "Tìm địa điểm • Mở sáng ${String.format("%.0f", mapCoverage)}% bản đồ" else searchQuery,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Quick jump to Hanoi / Da Nang / HCM
                IconButton(
                    onClick = {
                        zoom = 1.6f
                        panOffset = Offset(0f, -80f)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = "Trung tâm",
                        tint = LoveSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 7. Right Action FABs (GPS, Zoom +, Zoom -, Reset View)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // GPS Location Button
            FloatingActionButton(
                onClick = { requestGps() },
                containerColor = if (isLocating) LovePrimary else Color(0xEE1E2F46),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("map_gps_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.GpsFixed,
                    contentDescription = "Định vị hiện tại",
                    modifier = Modifier.size(22.dp)
                )
            }

            // Zoom In
            FloatingActionButton(
                onClick = { zoom = (zoom * 1.35f).coerceAtMost(8.0f) },
                containerColor = Color(0xEE1E2F46),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("map_zoom_in")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Phóng to",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Zoom Out
            FloatingActionButton(
                onClick = { zoom = (zoom / 1.35f).coerceAtLeast(0.8f) },
                containerColor = Color(0xEE1E2F46),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("map_zoom_out")
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Thu nhỏ",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Reset Map to default overview
            FloatingActionButton(
                onClick = {
                    zoom = 1.6f
                    panOffset = Offset(0f, -80f)
                },
                containerColor = Color(0xEE1E2F46),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("map_reset_view")
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = "Đặt lại toàn cảnh",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 8. Bottom Selected Memory or Dropped Pin Preview Card (Matches Screen 4/6 of mockup!)
        AnimatedVisibility(
            visible = selectedMemory != null || droppedPin != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 78.dp, start = 16.dp, end = 16.dp)
        ) {
            if (selectedMemory != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail with LoveImage
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(LoveSecondary)
                        ) {
                            LoveImage(
                                imageUri = selectedMemory.coverImageUri,
                                contentDescription = selectedMemory.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedMemory.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = LoveTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedMemory.locationName,
                                fontSize = 12.sp,
                                color = LoveTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${String.format("%.4f", selectedMemory.latitude)}, ${String.format("%.4f", selectedMemory.longitude)}",
                                fontSize = 11.sp,
                                color = LoveTextMuted
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { onViewMemoryDetail(selectedMemory) },
                            colors = ButtonDefaults.buttonColors(containerColor = LovePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("map_card_detail_btn")
                        ) {
                            Text("Chi tiết", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else if (droppedPin != null) {
                val (lat, lng) = droppedPin!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LoveCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(LoveSecondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = LovePrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = droppedPlaceName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = LoveTextPrimary
                                )
                                Text(
                                    text = "Tọa độ: ${String.format("%.4f", lat)}, ${String.format("%.4f", lng)}",
                                    fontSize = 12.sp,
                                    color = LoveTextSecondary
                                )
                            }
                            IconButton(onClick = { droppedPin = null }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Đóng",
                                    tint = LoveTextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                onContinueToForm(lat, lng, droppedPlaceName)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LovePrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("map_continue_to_form_btn")
                        ) {
                            Text(
                                "Tiếp tục tạo kỷ niệm tại đây",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoPinMarker(
    memory: MemoryEntity,
    isSelected: Boolean,
    offset: Offset,
    onClick: () -> Unit
) {
    val sizeDp = if (isSelected) 56.dp else 48.dp
    val elevation = if (isSelected) 10.dp else 6.dp

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (offset.x - 24.dp.value * 2).roundToInt(),
                    (offset.y - 24.dp.value * 2).roundToInt()
                )
            }
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular photo frame
            Box(
                modifier = Modifier
                    .size(sizeDp)
                    .shadow(elevation, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(
                        width = if (isSelected) 3.dp else 2.dp,
                        color = if (isSelected) LovePrimary else Color.White,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                LoveImage(
                    imageUri = memory.coverImageUri,
                    contentDescription = memory.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Mini heart pin badge
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(LovePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }

                // Photo count badge
                val photoCount = memory.imagesJson.split(",").filter { it.isNotBlank() }.size
                if (photoCount > 1) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(LovePrimary)
                            .border(1.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$photoCount",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Location title badge below pin
            Surface(
                modifier = Modifier.padding(top = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xE6101722),
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, LovePrimary.copy(alpha = 0.5f))
            ) {
                Text(
                    text = memory.title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun CandidatePinMarker(
    offset: Offset,
    pulseFraction: Float
) {
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (offset.x - 20.dp.value * 2).roundToInt(),
                    (offset.y - 20.dp.value * 2).roundToInt()
                )
            }
    ) {
        // Glowing radar pulse
        Canvas(modifier = Modifier.size(80.dp)) {
            val radius = 16.dp.toPx() + pulseFraction * 20.dp.toPx()
            val alpha = (1f - pulseFraction).coerceIn(0f, 1f)
            drawCircle(
                color = LovePrimary.copy(alpha = alpha * 0.5f),
                radius = radius,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.Center)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(LovePrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = "Vị trí mới",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Drawing vector geography (Vietnam coast, rivers, terrain)
private fun DrawScope.drawCoastlineAndGeography(
    geoToPixel: (Double, Double) -> Offset,
    zoom: Float
) {
    // S-curve of Vietnam geography path
    val vietnamPath = Path().apply {
        val p1 = geoToPixel(22.8, 105.0) // Ha Giang / North
        val p2 = geoToPixel(21.0, 105.8) // Hanoi
        val p3 = geoToPixel(20.0, 106.3) // Ninh Binh
        val p4 = geoToPixel(18.6, 105.7) // Vinh
        val p5 = geoToPixel(16.5, 107.5) // Hue
        val p6 = geoToPixel(16.0, 108.2) // Da Nang
        val p7 = geoToPixel(13.8, 109.2) // Quy Nhon
        val p8 = geoToPixel(12.2, 109.2) // Nha Trang
        val p9 = geoToPixel(10.8, 106.7) // TP. HCM
        val p10 = geoToPixel(9.2, 105.1) // Ca Mau
        val p11 = geoToPixel(10.0, 104.0) // Kien Giang
        val p12 = geoToPixel(11.9, 108.4) // Da Lat (Highlands)
        val p13 = geoToPixel(14.0, 108.0) // Pleiku
        val p14 = geoToPixel(21.5, 103.5) // Son La / Northwest

        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        lineTo(p4.x, p4.y)
        lineTo(p5.x, p5.y)
        lineTo(p6.x, p6.y)
        lineTo(p7.x, p7.y)
        lineTo(p8.x, p8.y)
        lineTo(p9.x, p9.y)
        lineTo(p10.x, p10.y)
        lineTo(p11.x, p11.y)
        lineTo(p12.x, p12.y)
        lineTo(p13.x, p13.y)
        lineTo(p14.x, p14.y)
        close()
    }

    // Land base fill
    drawPath(
        path = vietnamPath,
        color = MapLand,
        style = Fill
    )

    // Land border
    drawPath(
        path = vietnamPath,
        color = MapWater,
        style = Stroke(width = 3.dp.toPx())
    )

    // Red River & Mekong Delta stylized waterways
    val riverPath = Path().apply {
        val r1 = geoToPixel(21.8, 104.5)
        val r2 = geoToPixel(21.0, 105.8)
        val r3 = geoToPixel(20.3, 106.6)
        moveTo(r1.x, r1.y)
        quadraticTo(r2.x, r2.y, r3.x, r3.y)

        val m1 = geoToPixel(11.0, 105.5)
        val m2 = geoToPixel(10.3, 106.2)
        val m3 = geoToPixel(9.6, 106.7)
        moveTo(m1.x, m1.y)
        quadraticTo(m2.x, m2.y, m3.x, m3.y)
    }

    drawPath(
        path = riverPath,
        color = MapWater,
        style = Stroke(width = 2.5.dp.toPx())
    )
}

// Drawing highway and street grid network
private fun DrawScope.drawRoadNetwork(
    geoToPixel: (Double, Double) -> Offset,
    zoom: Float
) {
    // National Highway 1A
    val ql1a = Path().apply {
        val pts = listOf(
            geoToPixel(21.0, 105.8), // Hanoi
            geoToPixel(19.8, 105.8), // Thanh Hoa
            geoToPixel(18.6, 105.7), // Vinh
            geoToPixel(16.5, 107.5), // Hue
            geoToPixel(16.0, 108.2), // Da Nang
            geoToPixel(15.2, 108.8), // Quang Ngai
            geoToPixel(13.8, 109.2), // Quy Nhon
            geoToPixel(12.2, 109.2), // Nha Trang
            geoToPixel(11.6, 108.9), // Phan Rang
            geoToPixel(10.9, 108.1), // Phan Thiet
            geoToPixel(10.8, 106.7), // TP. HCM
            geoToPixel(10.0, 105.8), // Can Tho
            geoToPixel(9.2, 105.1)  // Ca Mau
        )
        moveTo(pts.first().x, pts.first().y)
        for (i in 1 until pts.size) {
            lineTo(pts[i].x, pts[i].y)
        }
    }

    drawPath(
        path = ql1a,
        color = MapRoadPrimary,
        style = Stroke(width = (1.5f * zoom).coerceIn(1f, 4f))
    )

    // Branch road to Da Lat
    val dalatBranch = Path().apply {
        val p1 = geoToPixel(11.6, 108.9)
        val p2 = geoToPixel(11.9, 108.4)
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
    }
    drawPath(
        path = dalatBranch,
        color = MapRoadSecondary,
        style = Stroke(width = (1.2f * zoom).coerceIn(1f, 3f))
    )
}

// 1. Revealed Fog of War Discs (Static on map, re-drawn only on pan/zoom/content changes)
private fun DrawScope.drawRevealedFogOfWarDiscs(
    memories: List<MemoryEntity>,
    geoToPixel: (Double, Double) -> Offset,
    zoom: Float
) {
    memories.forEach { memory ->
        val center = geoToPixel(memory.latitude, memory.longitude)
        val baseRadiusPx = (memory.revealRadiusMeters / 6f) * zoom
        val radiusPx = baseRadiusPx.coerceIn(50f, 300f)

        // Revealed illuminated circle with gradient border
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    MapRevealedLand.copy(alpha = 0.95f),
                    MapRevealedLand.copy(alpha = 0.7f),
                    MapGlowReveal.copy(alpha = 0.5f),
                    Color.Transparent
                ),
                center = center,
                radius = radiusPx
            ),
            center = center,
            radius = radiusPx
        )

        // Delicate luminous edge ring
        drawCircle(
            color = LovePrimary.copy(alpha = 0.6f),
            center = center,
            radius = radiusPx * 0.96f,
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )
        )
    }
}

// 2. Animated Beacon Rings (Lightweight 60fps radar animation)
private fun DrawScope.drawBeaconRings(
    memories: List<MemoryEntity>,
    geoToPixel: (Double, Double) -> Offset,
    zoom: Float,
    pulseFraction: Float
) {
    memories.forEach { memory ->
        val center = geoToPixel(memory.latitude, memory.longitude)
        val baseRadiusPx = (memory.revealRadiusMeters / 6f) * zoom
        val radiusPx = baseRadiusPx.coerceIn(50f, 300f)

        val pulseRadius = radiusPx + (pulseFraction * 32f * zoom).coerceAtMost(65f)
        val pulseAlpha = (1f - pulseFraction) * 0.45f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    LovePrimary.copy(alpha = pulseAlpha),
                    Color.Transparent
                ),
                center = center,
                radius = pulseRadius
            ),
            center = center,
            radius = pulseRadius
        )
    }
}
