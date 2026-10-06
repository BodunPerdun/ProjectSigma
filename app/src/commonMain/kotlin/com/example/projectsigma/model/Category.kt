package com.example.projectsigma.model

import com.example.projectsigma.i18n.AppLanguage
import com.example.projectsigma.i18n.AppLanguageManager
import kotlinx.serialization.Serializable

@Serializable
enum class EventCategory(
    val label: String,
    val iconName: String,
    val colorHex: String
) {
    PARTY("Party & Nightlife", "🎉", "#E91E63"),
    CULTURE("Arts & Culture", "🎨", "#9C27B0"),
    SPORTS("Sports & Fitness", "⚽", "#4CAF50"),
    FOOD("Food & Drinks", "🍕", "#FF9800"),
    MEETUP("Community & Meetup", "👥", "#2196F3"),
    EDUCATION("Workshop & Tech", "🎓", "#00BCD4"),
    OTHER("Other Events", "📍", "#607D8B");

    companion object {
        fun fromName(name: String): EventCategory {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: OTHER
        }
    }
}

val EventCategory.localizedLabel: String
    get() {
        val s = AppLanguageManager.strings
        return when (this) {
            EventCategory.PARTY -> s.catParty
            EventCategory.CULTURE -> s.catCulture
            EventCategory.SPORTS -> s.catSport
            EventCategory.FOOD -> s.catFood
            EventCategory.MEETUP -> s.catMeetup
            EventCategory.EDUCATION -> when (AppLanguageManager.currentLanguage.value) {
                AppLanguage.RUSSIAN -> "Мастер-класс & Технологии"
                AppLanguage.UKRAINIAN -> "Майстер-клас & Технології"
                AppLanguage.POLISH -> "Warsztaty & Technologia"
                AppLanguage.ENGLISH -> "Workshop & Tech"
            }
            EventCategory.OTHER -> when (AppLanguageManager.currentLanguage.value) {
                AppLanguage.RUSSIAN -> "Другие события"
                AppLanguage.UKRAINIAN -> "Інші події"
                AppLanguage.POLISH -> "Inne wydarzenia"
                AppLanguage.ENGLISH -> "Other Events"
            }
        }
    }
