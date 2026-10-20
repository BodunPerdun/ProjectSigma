package com.example.projectsigma.model

import com.example.projectsigma.i18n.AppLanguage
import com.example.projectsigma.i18n.AppLanguageManager
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val category: EventCategory,
    val latitude: Double,
    val longitude: Double,
    val dateTime: String,
    val photoUrl: String? = null,
    val createdById: String,
    val createdByName: String,
    val createdByAvatarUrl: String? = null,
    val participants: List<User> = emptyList(),
    val createdAtTimestamp: Long = 0L,
    val startTimestampMs: Long = 0L,
    val endTimestampMs: Long = 0L
)

val Event.localizedTitle: String
    get() = when (id) {
        "evt_1" -> if (AppLanguageManager.currentLanguage.value == AppLanguage.RUSSIAN) "Джазовый фестиваль на открытом воздухе" else "Open Air Jazz Festival"
        "evt_2" -> if (AppLanguageManager.currentLanguage.value == AppLanguage.RUSSIAN) "IT-митап и питч стартапов" else "Tech Startup Meetup & Pitch"
        "evt_3" -> if (AppLanguageManager.currentLanguage.value == AppLanguage.RUSSIAN) "Ночной маркет уличной еды" else "Street Food Night Market"
        "evt_4" -> if (AppLanguageManager.currentLanguage.value == AppLanguage.RUSSIAN) "Выставка современного искусства" else "Modern Art Gallery Exhibition"
        else -> title
    }

val Event.localizedDescription: String
    get() = when (id) {
        "evt_1" -> if (AppLanguageManager.currentLanguage.value == AppLanguage.RUSSIAN) "Насладитесь живым исполнением джаза под звездами с фудтраками и крафтовыми напитками." else "Enjoy live smooth jazz performance under the stars with food trucks and craft drinks."
        "evt_2" -> if (AppLanguageManager.currentLanguage.value == AppLanguage.RUSSIAN) "Общайтесь с основателями, инвесторами и инженерами. Питчинг-сессия начинается в 18:30." else "Network with co-founders, investors, and engineers. Pitching session starts at 18:30."
        "evt_3" -> if (AppLanguageManager.currentLanguage.value == AppLanguage.RUSSIAN) "Более 20 ремесленных продавцов еды представляют блюда мировой кухни, десерты и коктейли." else "Over 20 artisanal food vendors showcasing world cuisine, desserts, and cocktails."
        "evt_4" -> if (AppLanguageManager.currentLanguage.value == AppLanguage.RUSSIAN) "Эксклюзивная выставка цифровых арт-инсталляций и современной скульптуры." else "Exclusive exhibition featuring contemporary digital art installations and sculpture."
        else -> description
    }

val Event.formattedDateTime: String
    get() {
        if (startTimestampMs > 0L) {
            return try {
                val startInstant = Instant.fromEpochMilliseconds(startTimestampMs)
                val localStart = startInstant.toLocalDateTime(TimeZone.currentSystemDefault())
                val dayStr = "${localStart.dayOfMonth.toString().padStart(2, '0')} ${localStart.month.name.take(3)}"
                val startTimeStr = "${localStart.hour.toString().padStart(2, '0')}:${localStart.minute.toString().padStart(2, '0')}"

                if (endTimestampMs > startTimestampMs) {
                    val endInstant = Instant.fromEpochMilliseconds(endTimestampMs)
                    val localEnd = endInstant.toLocalDateTime(TimeZone.currentSystemDefault())
                    val endTimeStr = "${localEnd.hour.toString().padStart(2, '0')}:${localEnd.minute.toString().padStart(2, '0')}"
                    "$dayStr, $startTimeStr - $endTimeStr"
                } else {
                    "$dayStr, $startTimeStr"
                }
            } catch (e: Exception) {
                dateTime
            }
        }

        if (dateTime.isBlank()) return "Today, 18:00"
        val trimmed = dateTime.trim()

        if (trimmed.contains("T", ignoreCase = true)) {
            return try {
                val parts = trimmed.split("T", ignoreCase = true)
                val datePart = parts.getOrNull(0) ?: ""
                val timePart = parts.getOrNull(1)?.take(5) ?: "18:00"

                val dateFormatted = if (datePart.isNotBlank()) {
                    val dateComponents = datePart.split("-")
                    if (dateComponents.size == 3) {
                        val month = when (dateComponents[1]) {
                            "01" -> "Jan"; "02" -> "Feb"; "03" -> "Mar"; "04" -> "Apr"
                            "05" -> "May"; "06" -> "Jun"; "07" -> "Jul"; "08" -> "Aug"
                            "09" -> "Sep"; "10" -> "Oct"; "11" -> "Nov"; "12" -> "Dec"
                            else -> dateComponents[1]
                        }
                        "${dateComponents[2]} $month"
                    } else datePart
                } else "Today"

                "$dateFormatted, $timePart"
            } catch (e: Exception) {
                trimmed.replace("T", " ").take(16)
            }
        }

        return trimmed
    }

val Event.locationCityName: String
    get() = when {
        latitude in 51.2..51.7 && longitude in -0.6..0.3 -> "London"
        latitude in 50.3..50.6 && longitude in 30.3..30.7 -> "Kyiv"
        latitude in 52.1..52.4 && longitude in 20.8..21.2 -> "Warsaw"
        latitude in 48.8..48.9 && longitude in 2.2..2.5 -> "Paris"
        latitude in 52.4..52.6 && longitude in 13.2..13.6 -> "Berlin"
        latitude in 40.6..40.9 && longitude in -74.1..-73.8 -> "New York"
        else -> "Map Location (${(latitude * 1000.0).let { kotlin.math.round(it) } / 1000.0}, ${(longitude * 1000.0).let { kotlin.math.round(it) } / 1000.0})"
    }

fun Event.distanceFrom(userLat: Double?, userLng: Double?): String? {
    if (userLat == null || userLng == null) return null
    val lat1 = latitude
    val lon1 = longitude
    val lat2 = userLat
    val lon2 = userLng

    val r = 6371000.0
    val phi1 = kotlin.math.PI * lat1 / 180.0
    val phi2 = kotlin.math.PI * lat2 / 180.0
    val deltaPhi = kotlin.math.PI * (lat2 - lat1) / 180.0
    val deltaLambda = kotlin.math.PI * (lon2 - lon1) / 180.0

    val a = kotlin.math.sin(deltaPhi / 2.0) * kotlin.math.sin(deltaPhi / 2.0) +
            kotlin.math.cos(phi1) * kotlin.math.cos(phi2) *
            kotlin.math.sin(deltaLambda / 2.0) * kotlin.math.sin(deltaLambda / 2.0)
    val c = 2.0 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1.0 - a))
    val meters = (r * c).toInt()

    return if (meters < 1000) {
        "$meters m away"
    } else {
        val km = (meters / 100) / 10.0
        "$km km away"
    }
}
