package com.example.projectsigma.model

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
