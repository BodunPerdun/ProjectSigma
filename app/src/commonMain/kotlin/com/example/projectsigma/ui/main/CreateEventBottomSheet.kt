package com.example.projectsigma.ui.main

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsigma.i18n.AppLanguageManager
import com.example.projectsigma.model.Event
import com.example.projectsigma.model.EventCategory
import com.example.projectsigma.model.localizedLabel
import com.example.projectsigma.ui.components.AsyncEventImage
import com.example.projectsigma.ui.components.GalleryImagePickerButton
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventBottomSheet(
    location: Pair<Double, Double>,
    eventToEdit: Event? = null,
    onDismissRequest: () -> Unit,
    onCreateEvent: (title: String, description: String, category: EventCategory, dateTime: String, photoUrl: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Set skipPartiallyExpanded = false so the sheet opens at half-screen height over the map
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val haptic = LocalHapticFeedback.current

    val currentLanguage by AppLanguageManager.currentLanguage.collectAsState()
    val s = AppLanguageManager.strings

    // Pre-fill fields if editing existing event pin
    var title by remember(eventToEdit) { mutableStateOf(eventToEdit?.title ?: "") }
    var description by remember(eventToEdit) { mutableStateOf(eventToEdit?.description ?: "") }
    var selectedCategory by remember(eventToEdit) { mutableStateOf(eventToEdit?.category ?: EventCategory.MEETUP) }
    var photoUrl by remember(eventToEdit) { mutableStateOf<String?>(eventToEdit?.photoUrl) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val nowLocal = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
    val initialDateStr = "${nowLocal.dayOfMonth.toString().padStart(2, '0')}.${nowLocal.monthNumber.toString().padStart(2, '0')}.${nowLocal.year}"

    var selectedDateText by remember(eventToEdit) { mutableStateOf(initialDateStr) }

    var startHour by remember(eventToEdit) { mutableStateOf(nowLocal.hour) }
    var startMinute by remember(eventToEdit) { mutableStateOf((nowLocal.minute / 15) * 15) }
    var endHour by remember(eventToEdit) { mutableStateOf((nowLocal.hour + 2) % 24) }
    var endMinute by remember(eventToEdit) { mutableStateOf((nowLocal.minute / 15) * 15) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val startTimePickerState = rememberTimePickerState(initialHour = startHour, initialMinute = startMinute)
    val endTimePickerState = rememberTimePickerState(initialHour = endHour, initialMinute = endMinute)

    val isEditing = eventToEdit != null

    // Validation: Event duration must not be 0 minutes
    val isTimeIntervalValid = startHour != endHour || startMinute != endMinute
    val isCrossesMidnight = (endHour * 60 + endMinute) <= (startHour * 60 + startMinute)

    val isFormValid = isTimeIntervalValid && title.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = if (isEditing) s.editEventTitle else s.createEventTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(s.eventTitleLabel) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category selector
            Text(
                text = s.selectCategoryLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EventCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text("${cat.iconName} ${cat.localizedLabel}") },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date Picker Card
            Text(
                text = s.eventDateLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                onClick = { showDatePicker = true },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "📅 $selectedDateText",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Change", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Start / End Hours Selection Cards
            Text(
                text = s.timeIntervalLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = { showStartTimePicker = true },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(s.startHourLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatTime(startHour, startMinute),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    onClick = { showEndTimePicker = true },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(s.endHourLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isCrossesMidnight) "${formatTime(endHour, endMinute)} (+1d)" else formatTime(endHour, endMinute),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gallery Image Attachment Picker
            Text(
                text = s.attachPhotoLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            GalleryImagePickerButton(
                onImagePicked = { pickedUri ->
                    photoUrl = pickedUri
                }
            )

            photoUrl?.let { url ->
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncEventImage(
                        url = url,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s.photoAttachedMsg, fontSize = 12.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    TextButton(onClick = { photoUrl = null }) {
                        Text(s.removePhotoBtn, fontSize = 12.sp, color = Color.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(s.descriptionLabel) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Event title is required."
                    } else if (!isTimeIntervalValid) {
                        errorMessage = s.intervalOrderWarning
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val formattedDateTime = if (isCrossesMidnight) {
                            "$selectedDateText ${formatTime(startHour, startMinute)} - ${formatTime(endHour, endMinute)} (+1d)"
                        } else {
                            "$selectedDateText ${formatTime(startHour, startMinute)} - ${formatTime(endHour, endMinute)}"
                        }
                        onCreateEvent(title, description, selectedCategory, formattedDateTime, photoUrl)
                        onDismissRequest()
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = if (isEditing) s.saveChangesBtn else s.publishEventBtn,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Step 1: Material 3 Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val date = instant.toLocalDateTime(TimeZone.UTC).date
                        val day = date.dayOfMonth.toString().padStart(2, '0')
                        val month = date.monthNumber.toString().padStart(2, '0')
                        val year = date.year
                        selectedDateText = "$day.$month.$year"
                    }
                    showDatePicker = false
                }) {
                    Text(s.done)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Step 2: Start Time Picker Dialog
    if (showStartTimePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startHour = startTimePickerState.hour
                    startMinute = startTimePickerState.minute
                    showStartTimePicker = false
                }) {
                    Text(s.done)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = startTimePickerState)
        }
    }

    // Step 3: End Time Picker Dialog
    if (showEndTimePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endHour = endTimePickerState.hour
                    endMinute = endTimePickerState.minute
                    showEndTimePicker = false
                }) {
                    Text(s.done)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = endTimePickerState)
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val h = hour.toString().padStart(2, '0')
    val m = minute.toString().padStart(2, '0')
    return "$h:$m"
}
