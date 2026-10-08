package com.example.projectsigma.model

import com.example.projectsigma.i18n.AppLanguage
import com.example.projectsigma.i18n.AppLanguageManager
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
    val createdAtTimestamp: Long = 0L
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
