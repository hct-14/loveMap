package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CoupleProfileEntity
import com.example.data.LoveMapRepository
import com.example.data.MemoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit

data class LoveMilestone(
    val title: String,
    val targetDays: Long,
    val targetDate: LocalDate,
    val isPassed: Boolean,
    val isNext: Boolean,
    val daysRemaining: Long
)

data class LoveCounterData(
    val totalDays: Long,
    val years: Int,
    val months: Int,
    val days: Int,
    val weeks: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val startDate: LocalDate,
    val nextMilestone: LoveMilestone?,
    val milestones: List<LoveMilestone>
)

class LoveMapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LoveMapRepository

    val memories: StateFlow<List<MemoryEntity>>
    val coupleProfile: StateFlow<CoupleProfileEntity?>

    private val _selectedMemory = MutableStateFlow<MemoryEntity?>(null)
    val selectedMemory: StateFlow<MemoryEntity?> = _selectedMemory.asStateFlow()

    private val _targetMapLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val targetMapLocation: StateFlow<Pair<Double, Double>?> = _targetMapLocation.asStateFlow()

    private val _draftLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val draftLocation: StateFlow<Pair<Double, Double>?> = _draftLocation.asStateFlow()

    init {
        val database = AppDatabase.getInstance(application)
        repository = LoveMapRepository(database.loveMapDao())

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            repository.ensureInitialData()
        }

        // Use Eagerly so database records load immediately at app launch without composable subscription lag
        memories = repository.allMemories.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        coupleProfile = repository.coupleProfile.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )
    }

    // Dynamic Love Counter calculations with Eagerly pre-warm
    val counterData: StateFlow<LoveCounterData> = coupleProfile.combine(memories) { profile, _ ->
        calculateCounterData(profile?.startDateEpochDay)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        calculateCounterData(null)
    )

    fun calculateCounterData(startEpochDay: Long?): LoveCounterData {
        val today = LocalDate.now()
        val startDate = if (startEpochDay != null) LocalDate.ofEpochDay(startEpochDay) else LocalDate.of(2025, 1, 1)
        
        // Days count starts at 1 on start day
        val diffDays = ChronoUnit.DAYS.between(startDate, today)
        val totalDays = if (diffDays >= 0) diffDays + 1 else 1L
        
        val period = Period.between(startDate, today)
        val years = period.years.coerceAtLeast(0)
        val months = (years * 12 + period.months).coerceAtLeast(0)
        val weeks = totalDays / 7
        val hours = totalDays * 24
        val minutes = hours * 60
        val seconds = minutes * 60

        // Milestone benchmarks
        val targetDayList = listOf(100L, 200L, 300L, 365L, 400L, 500L, 730L, 1000L, 1095L, 1500L)
        var nextFound = false

        val milestones = targetDayList.map { target ->
            val targetDate = startDate.plusDays(target - 1)
            val isPassed = totalDays > target
            val daysRemaining = (target - totalDays).coerceAtLeast(0)
            val isNext = !isPassed && !nextFound
            if (isNext) nextFound = true

            val label = when (target) {
                365L -> "1 năm yêu nhau (365 ngày)"
                730L -> "2 năm yêu nhau (730 ngày)"
                1095L -> "3 năm yêu nhau (1095 ngày)"
                else -> "Kỷ niệm $target ngày"
            }

            LoveMilestone(
                title = label,
                targetDays = target,
                targetDate = targetDate,
                isPassed = isPassed,
                isNext = isNext,
                daysRemaining = daysRemaining
            )
        }

        val nextMilestone = milestones.firstOrNull { it.isNext }

        return LoveCounterData(
            totalDays = totalDays,
            years = years,
            months = months,
            days = totalDays.toInt(),
            weeks = weeks,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            startDate = startDate,
            nextMilestone = nextMilestone,
            milestones = milestones
        )
    }

    fun selectMemory(memory: MemoryEntity?) {
        _selectedMemory.value = memory
    }

    fun selectMemoryById(id: Long) {
        viewModelScope.launch {
            val memory = repository.getMemoryById(id)
            _selectedMemory.value = memory
        }
    }

    fun setTargetMapLocation(lat: Double, lng: Double) {
        _targetMapLocation.value = Pair(lat, lng)
    }

    fun clearTargetMapLocation() {
        _targetMapLocation.value = null
    }

    fun setDraftLocation(lat: Double, lng: Double) {
        _draftLocation.value = Pair(lat, lng)
    }

    fun clearDraftLocation() {
        _draftLocation.value = null
    }

    fun addMemory(
        title: String,
        locationName: String,
        dateEpochDay: Long,
        latitude: Double,
        longitude: Double,
        revealRadiusMeters: Int,
        description: String,
        coverImageUri: String,
        imagesJson: String,
        mood: String,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val entity = MemoryEntity(
                title = title.trim(),
                locationName = locationName.trim(),
                dateEpochDay = dateEpochDay,
                latitude = latitude,
                longitude = longitude,
                revealRadiusMeters = revealRadiusMeters,
                description = description.trim(),
                coverImageUri = coverImageUri.ifBlank { "hero_couple_sunset" },
                imagesJson = imagesJson.ifBlank { "hero_couple_sunset" },
                mood = mood
            )
            val id = repository.addMemory(entity)
            _selectedMemory.value = entity.copy(id = id)
            onSuccess(id)
        }
    }

    fun updateMemory(memory: MemoryEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateMemory(memory)
            _selectedMemory.value = memory
            onSuccess()
        }
    }

    fun deleteMemory(id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteMemory(id)
            if (_selectedMemory.value?.id == id) {
                _selectedMemory.value = null
            }
            onSuccess()
        }
    }

    fun updateCoupleProfile(
        partner1Name: String,
        partner2Name: String,
        startDateEpochDay: Long,
        fogRadiusMeters: Int,
        notificationsEnabled: Boolean,
        themeColorName: String,
        partner1Avatar: String? = null,
        partner2Avatar: String? = null
    ) {
        viewModelScope.launch {
            val current = coupleProfile.value ?: return@launch
            val updated = current.copy(
                partner1Name = partner1Name,
                partner2Name = partner2Name,
                partner1Avatar = partner1Avatar ?: current.partner1Avatar,
                partner2Avatar = partner2Avatar ?: current.partner2Avatar,
                startDateEpochDay = startDateEpochDay,
                fogRadiusMeters = fogRadiusMeters,
                notificationsEnabled = notificationsEnabled,
                themeColorName = themeColorName
            )
            repository.updateProfile(updated)
        }
    }
}
