package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

class LoveMapRepository(private val dao: LoveMapDao) {

    val allMemories: Flow<List<MemoryEntity>> = dao.getAllMemories()
    val coupleProfile: Flow<CoupleProfileEntity?> = dao.getProfileFlow()

    suspend fun ensureInitialData() {
        val existingProfile = dao.getProfile()
        if (existingProfile == null) {
            val defaultStart = LocalDate.of(2025, 1, 1).toEpochDay()
            dao.saveProfile(
                CoupleProfileEntity(
                    id = 1,
                    partner1Name = "Anh",
                    partner2Name = "Em",
                    partner1Avatar = "avatar_anh",
                    partner2Avatar = "avatar_em",
                    startDateEpochDay = defaultStart,
                    fogRadiusMeters = 300,
                    notificationsEnabled = true,
                    themeColorName = "Hồng phấn",
                    pairCode = "LOVE-8899"
                )
            )
        }

        val count = dao.getAllMemories().firstOrNull()?.size ?: 0
        if (count == 0) {
            val samples = listOf(
                MemoryEntity(
                    title = "Hồ Gươm",
                    locationName = "Hà Nội, Việt Nam",
                    dateEpochDay = LocalDate.of(2026, 2, 14).toEpochDay(),
                    latitude = 21.0285,
                    longitude = 105.8542,
                    revealRadiusMeters = 300,
                    description = "Valentine đầu tiên của chúng mình. Dạo quanh hồ, ăn kem và chụp thật nhiều ảnh đẹp 💕",
                    coverImageUri = "hero_couple_sunset",
                    imagesJson = "hero_couple_sunset,avatar_anh,avatar_em",
                    mood = "Lãng mạn 💕"
                ),
                MemoryEntity(
                    title = "Bà Nà Hills & Biển Mỹ Khê",
                    locationName = "Đà Nẵng, Việt Nam",
                    dateEpochDay = LocalDate.of(2025, 6, 1).toEpochDay(),
                    latitude = 16.0544,
                    longitude = 108.2022,
                    revealRadiusMeters = 300,
                    description = "Ngắm bình minh trên biển Mỹ Khê và đi cáp treo lên đỉnh Bà Nà cùng nhau 🌊",
                    coverImageUri = "hero_couple_sunset",
                    imagesJson = "hero_couple_sunset,avatar_em",
                    mood = "Hạnh phúc 🥰"
                ),
                MemoryEntity(
                    title = "Hồ Xuân Hương",
                    locationName = "Đà Lạt, Lâm Đồng",
                    dateEpochDay = LocalDate.of(2025, 4, 30).toEpochDay(),
                    latitude = 11.9404,
                    longitude = 108.4583,
                    revealRadiusMeters = 300,
                    description = "Chuyến đi Đà Lạt mộng mơ. Không khí se lạnh, cùng nắm tay đi dạo quanh hồ và uống sữa đậu nành nóng ☕",
                    coverImageUri = "hero_couple_sunset",
                    imagesJson = "hero_couple_sunset,avatar_anh",
                    mood = "Bình yên ☕"
                ),
                MemoryEntity(
                    title = "Phố đi bộ Nguyễn Huệ",
                    locationName = "TP. Hồ Chí Minh",
                    dateEpochDay = LocalDate.of(2025, 8, 20).toEpochDay(),
                    latitude = 10.7769,
                    longitude = 106.7009,
                    revealRadiusMeters = 300,
                    description = "Cùng ngắm thành phố lên đèn rực rỡ và thưởng thức trà sữa đêm hè rộn rã ✨",
                    coverImageUri = "hero_couple_sunset",
                    imagesJson = "hero_couple_sunset,avatar_anh,avatar_em",
                    mood = "Vui vẻ 😊"
                )
            )
            dao.insertAllMemories(samples)
        }
    }

    suspend fun getMemoryById(id: Long): MemoryEntity? = dao.getMemoryById(id)

    suspend fun addMemory(memory: MemoryEntity): Long = dao.insertMemory(memory)

    suspend fun updateMemory(memory: MemoryEntity) = dao.updateMemory(memory)

    suspend fun deleteMemory(memoryId: Long) = dao.deleteMemoryById(memoryId)

    suspend fun updateProfile(profile: CoupleProfileEntity) = dao.saveProfile(profile)
}
