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
    val noNotificationsMsg: String
)

val StringsRu = AppStrings(
    catAll = "Все события",
    catParty = "Вечеринка",
    catMeetup = "Встреча",
    catFood = "Еда & Напитки",
    catCulture = "Культура & Арт",
    catSport = "Спорт & Фитнес",

    friendsBtn = "👥 Друзья",
    settingsTitle = "⚙️ Настройки приложения",
    appLanguage = "🌐 Язык интерфейса",
    done = "Готово",

    createEventTitle = "📍 Создать метку события",
    editEventTitle = "✏️ Редактировать событие",
    eventTitleLabel = "Название события *",
    selectCategoryLabel = "Выберите категорию",
    eventDateLabel = "Дата проведения",
    timeIntervalLabel = "Интервал времени (Часы)",
    startHourLabel = "Время начала *",
    endHourLabel = "Время окончания *",
    attachPhotoLabel = "Обложка события",
    pickPhotoBtn = "🖼️ Выбрать фото из галереи",
    photoAttachedMsg = "Фото прикреплено!",
    removePhotoBtn = "Удалить фото",
    descriptionLabel = "Описание события",
    publishEventBtn = "Опубликовать на карте",
    saveChangesBtn = "Сохранить изменения",
    resetToNowBtn = "⚡ Сбросить на текущее время",
    pastTimeWarning = "⚠️ Время начала не может быть в прошлом!",
    intervalOrderWarning = "⚠️ Время окончания должно быть позже времени начала",

    organizedBy = "Организатор",
    participantsTitle = "👥 Участники",
    noParticipantsMsg = "Пока нет участников. Будьте первыми!",
    viewAllBtn = "Все",
    editDetailsBtn = "✏️ Редактировать",
    deletePinBtn = "🗑️ Удалить метку",
    joinEventBtn = "🎉 Пойти на событие",
    leaveEventBtn = "✓ Вы идёте (Нажмите чтобы отменить)",
    yourEventBadge = "⭐ Ваше событие",

    createdEventsTitle = "Создано событий",
    friendsCountTitle = "Друзей",
    visibleNearbyTitle = "📡 Видим для людей рядом",
    visibleNearbySubtitle = "Разрешить людям поблизости находить ваш профиль в LocaPop",
    publicSocialsTitle = "🌐 Публичные ссылки на соцсети",
    publicSocialsSubtitle = "Показывать ваши Instagram и Telegram другим пользователям в LocaPop",
    editSocialsBtn = "✏️ Изменить соцсети",
    linkSocialsTitle = "✏️ Ссылки на социальные сети",
    signOutBtn = "Выйти из аккаунта",
    myFriendsTab = "Мои друзья",
    peopleNearbyTab = "📡 Люди рядом",
    addFriendBtn = "➕ Добавить",
    removeFriendBtn = "Удалить",
    friendBadge = "✓ В друзьях",
    hiddenNearbyWarning = "⚠️ Вы скрыты от поиска людей рядом",
    enableBtn = "Включить",
    socialsPrivateMsg = "🔒 Ссылки на соцсети скрыты настройками приватности",
    closeBtn = "Закрыть",

    clusterTitle = "📍 Кластер событий",
    clusterSubtitle = "Выберите событие для просмотра деталей:",
    friendsSheetTitle = "👥 Друзья и люди рядом",
    noFriendsYetMsg = "👥 Друзья пока не добавлены",
    noFriendsSubtitle = "Перейдите на вкладку '📡 Люди рядом', чтобы найти друзей поблизи!",
    peopleNearbySubtitle = "Люди в вашем районе:",
    tapToViewProfile = "Нажмите для просмотра профиля",
    accountStatusActive = "Активен",
    accountStatusTitle = "Статус аккаунта",
    eventsAttended = "Посещено событий",
    noSocialsMsg = "Социальные сети не указаны",

    bioTitle = "О себе",
    editBioBtn = "✏️ Изменить о себе",
    noBioMsg = "Информация о себе пока не добавлена.",
    saveBioBtn = "Сохранить информацию",

    notificationsTitle = "🔔 Уведомления",
    acceptBtn = "✓ Принять",
    declineBtn = "✕ Отклонить",
    noNotificationsMsg = "У вас нет новых уведомлений"
)

val StringsUk = AppStrings(
    catAll = "Усі події",
    catParty = "Вечірка",
    catMeetup = "Зустріч",
    catFood = "Їжа & Напої",
    catCulture = "Культура & Арт",
    catSport = "Спорт & Фітнес",

    friendsBtn = "👥 Друзі",
    settingsTitle = "⚙️ Налаштування програми",
    appLanguage = "🌐 Мова інтерфейсу",
    done = "Готово",

    createEventTitle = "📍 Створити мітку події",
    editEventTitle = "✏️ Редагувати подію",
    eventTitleLabel = "Назва події *",
    selectCategoryLabel = "Оберіть категорію",
    eventDateLabel = "Дата проведення",
    timeIntervalLabel = "Інтервал часу (Години)",
    startHourLabel = "Час початку *",
    endHourLabel = "Час закінчення *",
    attachPhotoLabel = "Обкладинка події",
    pickPhotoBtn = "🖼️ Обрати фото з галереї",
    photoAttachedMsg = "Фото прикріплено!",
    removePhotoBtn = "Видалити фото",
    descriptionLabel = "Опис події",
    publishEventBtn = "Опублікувати на карті",
    saveChangesBtn = "Зберегти зміни",
    resetToNowBtn = "⚡ Скинути на поточний час",
    pastTimeWarning = "⚠️ Час початку не може бути в минулому!",
    intervalOrderWarning = "⚠️ Час закінчення має бути пізніше часу початку",

    organizedBy = "Організатор",
    participantsTitle = "👥 Учасники",
    noParticipantsMsg = "Поки немає учасників. Будьте першими!",
    viewAllBtn = "Усі",
    editDetailsBtn = "✏️ Редагувати",
    deletePinBtn = "🗑️ Видалити мітку",
    joinEventBtn = "🎉 Піти на подію",
    leaveEventBtn = "✓ Ви йдете (Натисніть щоб скасувати)",
    yourEventBadge = "⭐ Ваша подія",

    createdEventsTitle = "Створено подій",
    friendsCountTitle = "Друзів",
    visibleNearbyTitle = "📡 Видимий для людей поруч",
    visibleNearbySubtitle = "Дозволити людям поблизу знаходити ваш профіль у LocaPop",
    publicSocialsTitle = "🌐 Публічні посилання на соцмережі",
    publicSocialsSubtitle = "Показувати ваші Instagram та Telegram іншим користувачам у LocaPop",
    editSocialsBtn = "✏️ Змінити соцмережі",
    linkSocialsTitle = "✏️ Посилання на соціальні мережі",
    signOutBtn = "Вийти з акаунта",
    myFriendsTab = "Мої друзі",
    peopleNearbyTab = "📡 Люди поруч",
    addFriendBtn = "➕ Додати",
    removeFriendBtn = "Видалити",
    friendBadge = "✓ У друзях",
    hiddenNearbyWarning = "⚠️ Ви приховані від пошуку людей поруч",
    enableBtn = "Увімкнути",
    socialsPrivateMsg = "🔒 Посилання на соцмережі приховані налаштуваннями приватності",
    closeBtn = "Закрити",

    clusterTitle = "📍 Кластер подій",
    clusterSubtitle = "Оберіть подію для перегляду деталей:",
    friendsSheetTitle = "👥 Друзі та люди поруч",
    noFriendsYetMsg = "👥 Друзі поки не додані",
    noFriendsSubtitle = "Перейдіть на вкладку '📡 Люди поруч', щоб знайти друзів поблизу!",
    peopleNearbySubtitle = "Люди у вашому районі:",
    tapToViewProfile = "Натисніть для перегляду профілю",
    accountStatusActive = "Активний",
    accountStatusTitle = "Статус акаунта",
    eventsAttended = "Відвідано подій",
    noSocialsMsg = "Соціальні мережі не вказані",

    bioTitle = "Про себе",
    editBioBtn = "✏️ Змінити про себе",
    noBioMsg = "Інформація про себе поки не додана.",
    saveBioBtn = "Зберегти інформацію",

    notificationsTitle = "🔔 Повідомлення",
    acceptBtn = "✓ Прийняти",
    declineBtn = "✕ Відхилити",
    noNotificationsMsg = "У вас немає нових повідомлень"
)

val StringsPl = AppStrings(
    catAll = "Wszystkie wydarzenia",
    catParty = "Impreza",
    catMeetup = "Spotkanie",
    catFood = "Jedzenie & Napoje",
    catCulture = "Kultura & Sztuka",
    catSport = "Sport & Fitness",

    friendsBtn = "👥 Znajomi",
    settingsTitle = "⚙️ Ustawienia aplikacji",
    appLanguage = "🌐 Język aplikacji",
    done = "Gotowe",

    createEventTitle = "📍 Utwórz znacznik wydarzenia",
    editEventTitle = "✏️ Edytuj wydarzenie",
    eventTitleLabel = "Tytuł wydarzenia *",
    selectCategoryLabel = "Wybierz kategorię",
    eventDateLabel = "Data wydarzenia",
    timeIntervalLabel = "Przedział czasowy (Godziny)",
    startHourLabel = "Godzina rozpoczęcia *",
    endHourLabel = "Godzina zakończenia *",
    attachPhotoLabel = "Zdjęcie okładki",
    pickPhotoBtn = "🖼️ Wybierz zdjęcie z galerii",
    photoAttachedMsg = "Zdjęcie dołączone!",
    removePhotoBtn = "Usuń zdjęcie",
    descriptionLabel = "Opis wydarzenia",
    publishEventBtn = "Opublikuj na mapie",
    saveChangesBtn = "Zapisz zmiany",
    resetToNowBtn = "⚡ Zresetuj do teraz",
    pastTimeWarning = "⚠️ Czas rozpoczęcia nie może być w przeszłości!",
    intervalOrderWarning = "⚠️ Godzina zakończenia musi być późniejsza niż rozpoczęcia",

    organizedBy = "Organizator",
    participantsTitle = "👥 Uczestnicy",
    noParticipantsMsg = "Brak uczestników. Bądź pierwszy!",
    viewAllBtn = "Wszystkie",
    editDetailsBtn = "✏️ Edytuj",
    deletePinBtn = "🗑️ Usuń znacznik",
    joinEventBtn = "🎉 Dołącz do wydarzenia",
    leaveEventBtn = "✓ Dołączono (Dotknij, aby opuścić)",
    yourEventBadge = "⭐ Twoje wydarzenie",

    createdEventsTitle = "Utworzone wydarzenia",
    friendsCountTitle = "Znajomi",
    visibleNearbyTitle = "📡 Widoczny dla osób w pobliżu",
    visibleNearbySubtitle = "Zezwól osobom w pobliżu na odkrycie Twojego profilu w LocaPop",
    publicSocialsTitle = "🌐 Publiczne linki społecznościowe",
    publicSocialsSubtitle = "Pokaż swoje linki Instagram i Telegram innym użytkownikom w LocaPop",
    editSocialsBtn = "✏️ Edytuj profile",
    linkSocialsTitle = "✏️ Połącz konta społecznościowe",
    signOutBtn = "Wyloguj się",
    myFriendsTab = "Moi znajomi",
    peopleNearbyTab = "📡 Osoby w pobliżu",
    addFriendBtn = "➕ Dodaj",
    removeFriendBtn = "Usuń",
    friendBadge = "✓ Znajomy",
    hiddenNearbyWarning = "⚠️ Jesteś ukryty przed wyszukiwaniem w pobliżu",
    enableBtn = "Włącz",
    socialsPrivateMsg = "🔒 Linki społecznościowe są prywatne",
    closeBtn = "Zamknij",

    clusterTitle = "📍 Klaster wydarzeń",
    clusterSubtitle = "Wybierz wydarzenie, aby zobaczyć szczegóły:",
    friendsSheetTitle = "👥 Znajomi i osoby w pobliżu",
    noFriendsYetMsg = "👥 Brak dodanych znajomych",
    noFriendsSubtitle = "Przejdź do zakładki '📡 Osoby w pobliżu', aby znaleźć znajomych!",
    peopleNearbySubtitle = "Osoby w Twojej okolicy:",
    tapToViewProfile = "Dotknij, aby zobaczyć profil",
    accountStatusActive = "Aktywny",
    accountStatusTitle = "Stan konta",
    eventsAttended = "Udział w wydarzeniach",
    noSocialsMsg = "Brak podanych linków społecznościowych",

    bioTitle = "O sobie",
    editBioBtn = "✏️ Edytuj o sobie",
    noBioMsg = "Brak informacji o sobie.",
    saveBioBtn = "Zapisz informacje",

    notificationsTitle = "🔔 Powiadomienia",
    acceptBtn = "✓ Zaakceptuj",
    declineBtn = "✕ Odrzuć",
    noNotificationsMsg = "Brak nowych powiadomień"
)

val StringsEn = AppStrings(
    catAll = "All Events",
    catParty = "Party",
    catMeetup = "Meetup",
    catFood = "Food & Drinks",
    catCulture = "Culture & Art",
    catSport = "Sports & Fitness",

    friendsBtn = "👥 Friends",
    settingsTitle = "⚙️ App Settings",
    appLanguage = "🌐 App Language",
    done = "Done",

    createEventTitle = "📍 Create Event Pin",
    editEventTitle = "✏️ Edit Event Details",
    eventTitleLabel = "Event Title *",
    selectCategoryLabel = "Select Category",
    eventDateLabel = "Event Date",
    timeIntervalLabel = "Event Time Interval (Hours)",
    startHourLabel = "Start Hour *",
    endHourLabel = "End Hour *",
    attachPhotoLabel = "Attach Event Cover Photo",
    pickPhotoBtn = "🖼️ Pick Photo from Device Gallery",
    photoAttachedMsg = "Photo attached!",
    removePhotoBtn = "Remove Photo",
    descriptionLabel = "Description",
    publishEventBtn = "Publish Event on Map",
    saveChangesBtn = "Save Changes",
    resetToNowBtn = "⚡ Reset to Current Time",
    pastTimeWarning = "⚠️ Selected Start Time is in the past!",
    intervalOrderWarning = "⚠️ End hour cannot be earlier than or equal to Start hour",

    organizedBy = "Organized by",
    participantsTitle = "👥 Participants",
    noParticipantsMsg = "No participants yet. Be the first to join!",
    viewAllBtn = "View All",
    editDetailsBtn = "✏️ Edit Details",
    deletePinBtn = "🗑️ Delete Pin",
    joinEventBtn = "🎉 Join Event",
    leaveEventBtn = "✓ Joined (Tap to Leave)",
    yourEventBadge = "⭐ Your Event",

    createdEventsTitle = "Created Events",
    friendsCountTitle = "Friends",
    visibleNearbyTitle = "📡 Visible to People Nearby",
    visibleNearbySubtitle = "Allow people nearby on LocaPop to discover your profile",
    publicSocialsTitle = "🌐 Public Social Media Links",
    publicSocialsSubtitle = "Allow other users on LocaPop to see your Instagram & Telegram links",
    editSocialsBtn = "✏️ Edit Socials",
    linkSocialsTitle = "✏️ Link Social Media Handles",
    signOutBtn = "Sign Out",
    myFriendsTab = "My Friends",
    peopleNearbyTab = "📡 People Nearby",
    addFriendBtn = "➕ Add",
    removeFriendBtn = "Remove",
    friendBadge = "✓ Friend",
    hiddenNearbyWarning = "⚠️ Hidden from People Nearby",
    enableBtn = "Enable",
    socialsPrivateMsg = "🔒 Social links are private",
    closeBtn = "Close",

    clusterTitle = "📍 Events Cluster",
    clusterSubtitle = "Select an event to view full details:",
    friendsSheetTitle = "👥 Friends & People Nearby",
    noFriendsYetMsg = "👥 No friends added yet",
    noFriendsSubtitle = "Switch to '📡 People Nearby' tab to discover friends around you!",
    peopleNearbySubtitle = "People in your immediate area:",
    tapToViewProfile = "Tap to view profile",
    accountStatusActive = "Active",
    accountStatusTitle = "Account Status",
    eventsAttended = "Events Attended",
    noSocialsMsg = "No social links provided",

    bioTitle = "About Me",
    editBioBtn = "✏️ Edit Bio",
    noBioMsg = "No bio description added yet.",
    saveBioBtn = "Save Bio",

    notificationsTitle = "🔔 Notifications",
    acceptBtn = "✓ Accept",
    declineBtn = "✕ Decline",
    noNotificationsMsg = "You have no new notifications"
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
