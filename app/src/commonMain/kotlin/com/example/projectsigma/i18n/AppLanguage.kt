package com.example.projectsigma.i18n

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val displayName: String, val flagEmoji: String) {
    ENGLISH("en", "English", "🇺🇸"),
    RUSSIAN("ru", "Русский", "🇷🇺"),
    UKRAINIAN("uk", "Українська", "🇺🇦"),
    POLISH("pl", "Polski", "🇵🇱")
}

data class AppStrings(
    val catAll: String,
    val catParty: String,
    val catMeetup: String,
    val catFood: String,
    val catCulture: String,
    val catSport: String,

    val friendsBtn: String,
    val settingsTitle: String,
    val appLanguage: String,
    val done: String,

    val createEventTitle: String,
    val editEventTitle: String,
    val eventTitleLabel: String,
    val selectCategoryLabel: String,
    val eventDateLabel: String,
    val timeIntervalLabel: String,
    val startHourLabel: String,
    val endHourLabel: String,
    val attachPhotoLabel: String,
    val pickPhotoBtn: String,
    val photoAttachedMsg: String,
    val removePhotoBtn: String,
    val descriptionLabel: String,
    val publishEventBtn: String,
    val saveChangesBtn: String,
    val resetToNowBtn: String,
    val pastTimeWarning: String,
    val intervalOrderWarning: String,

    val organizedBy: String,
    val participantsTitle: String,
    val noParticipantsMsg: String,
    val viewAllBtn: String,
    val editDetailsBtn: String,
    val deletePinBtn: String,
    val joinEventBtn: String,
    val leaveEventBtn: String,
    val yourEventBadge: String,

    val createdEventsTitle: String,
    val friendsCountTitle: String,
    val visibleNearbyTitle: String,
    val visibleNearbySubtitle: String,
    val publicSocialsTitle: String,
    val publicSocialsSubtitle: String,
    val editSocialsBtn: String,
    val linkSocialsTitle: String,
    val signOutBtn: String,
    val myFriendsTab: String,
    val peopleNearbyTab: String,
    val addFriendBtn: String,
    val removeFriendBtn: String,
    val friendBadge: String,
    val hiddenNearbyWarning: String,
    val enableBtn: String,
    val socialsPrivateMsg: String,
    val closeBtn: String,

    val clusterTitle: String,
    val clusterSubtitle: String,
    val friendsSheetTitle: String,
    val noFriendsYetMsg: String,
    val noFriendsSubtitle: String,
    val peopleNearbySubtitle: String,
    val tapToViewProfile: String,
    val accountStatusActive: String,
    val accountStatusTitle: String,
    val eventsAttended: String,
    val noSocialsMsg: String,

    val bioTitle: String,
    val editBioBtn: String,
    val noBioMsg: String,
    val saveBioBtn: String,

    val notificationsTitle: String,
    val acceptBtn: String,
    val declineBtn: String,
    val noNotificationsMsg: String,

    val eventCoAttendeesTab: String,
    val eventCoAttendeesSubtitle: String,
    val noCoAttendeesMsg: String,
    val attendedTogetherText: String,

    val requestSentMsg: String,
    val pendingRequestBtn: String,

    val darkThemeTitle: String,
    val darkThemeSubtitle: String,

    val googleSignInTitle: String,
    val googleSignInSubtitle: String,
    val chooseOtherAccountBtn: String,
    val openGoogleWebBtn: String
)

object AppLanguageManager {
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private var persistenceHandler: ((AppLanguage) -> Unit)? = null

    val strings: AppStrings
        get() = when (_currentLanguage.value) {
            AppLanguage.ENGLISH -> StringsEn
            AppLanguage.RUSSIAN -> StringsRu
            AppLanguage.UKRAINIAN -> StringsUk
            AppLanguage.POLISH -> StringsPl
        }

    fun initPersistence(savedCode: String?, handler: (AppLanguage) -> Unit) {
        persistenceHandler = handler
        if (!savedCode.isNullOrBlank()) {
            val restored = AppLanguage.entries.find { it.code.equals(savedCode, ignoreCase = true) }
            if (restored != null) {
                _currentLanguage.value = restored
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        persistenceHandler?.invoke(language)
    }
}
