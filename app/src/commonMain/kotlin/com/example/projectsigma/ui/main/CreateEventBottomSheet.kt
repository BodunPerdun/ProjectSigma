package com.example.projectsigma.ui.main

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
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
import com.example.projectsigma.model.Event
import com.example.projectsigma.model.EventCategory
import com.example.projectsigma.ui.components.AsyncEventImage
import com.example.projectsigma.ui.components.GalleryImagePickerButton
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.round

private fun round4(value: Double): Double = round(value * 10000.0) / 10000.0

private fun formatTime(hour: Int, minute: Int): String {
    val h = hour.toString().padStart(2, '0')
    val m = minute.toString().padStart(2, '0')
    return "$h:$m"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventBottomSheet(
    location: Pair<Double, Double>,
    eventToEdit: Event? = null,
    onDismissRequest: () -> Unit,
    onCreateEvent: (title: String, description: String, category: EventCategory, dateTime: String, photoUrl: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    var title by remember { mutableStateOf(eventToEdit?.title ?: "") }
    var description by remember { mutableStateOf(eventToEdit?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(eventToEdit?.category ?: EventCategory.MEETUP) }
    var photoUrl by remember { mutableStateOf<String?>(eventToEdit?.photoUrl) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Separated Date and Hour States
    var selectedDateText by remember { mutableStateOf("05.10.2026") }
    var startHour by remember { mutableStateOf(19) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(21) }
    var endMinute by remember { mutableStateOf(0) }

    // Parse initial dateTime if editing
    remember(eventToEdit) {
        if (eventToEdit != null && eventToEdit.dateTime.contains(" ")) {
            val parts = eventToEdit.dateTime.split(" ")
            if (parts.isNotEmpty()) {
                selectedDateText = parts[0]
            }
            val timePart = eventToEdit.dateTime.substringAfter(selectedDateText).trim()
            if (timePart.contains("-")) {
                val times = timePart.split("-")
                val start = times[0].trim()
                val end = times[1].trim()
                val startH = start.substringBefore(":").toIntOrNull()
                val startM = start.substringAfter(":").toIntOrNull()
                val endH = end.substringBefore(":").toIntOrNull()
                val endM = end.substringAfter(":").toIntOrNull()

                if (startH != null) startHour = startH
                if (startM != null) startMinute = startM
                if (endH != null) endHour = endH
                if (endM != null) endMinute = endM
            }
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val startTimePickerState = rememberTimePickerState(initialHour = startHour, initialMinute = startMinute)
    val endTimePickerState = rememberTimePickerState(initialHour = endHour, initialMinute = endMinute)

    val isEditing = eventToEdit != null

    // Validation: Total Minutes Comparison
    val startTotalMinutes = startHour * 60 + startMinute
    val endTotalMinutes = endHour * 60 + endMinute
    val isTimeIntervalValid = endTotalMinutes > startTotalMinutes

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
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
                text = if (isEditing) "✏️ Edit Event Details" else "📍 Create Event Pin",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = "Pinned at Lat: ${round4(location.first)}, Lng: ${round4(location.second)}",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category selector
            Text(
                text = "Select Category",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EventCategory.entries.forEach { cat ->
                    val selected = cat == selectedCategory
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategory = cat },
                        label = { Text("${cat.iconName} ${cat.label}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SEPARATED FIELD 1: Date Field (Entire Box Clickable)
            Text(
                text = "Event Date",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            ) {
                OutlinedTextField(
                    value = selectedDateText,
                    onValueChange = {},
                    label = { Text("Date (DD.MM.YYYY) *") },
                    singleLine = true,
                    readOnly = true,
                    enabled = false,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Unspecified,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        Text("📅 Pick Date", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SEPARATED FIELD 2 & 3: Hours Interval Fields (Entire Boxes Clickable)
            Text(
                text = "Event Time Interval (Hours)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start Hour Field Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showStartTimePicker = true }
                ) {
                    OutlinedTextField(
                        value = formatTime(startHour, startMinute),
                        onValueChange = {},
                        label = { Text("Start Hour *") },
                        singleLine = true,
                        readOnly = true,
                        enabled = false,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Unspecified,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Text("⏰", fontSize = 14.sp)
                        }
                    )
                }

                // End Hour Field Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showEndTimePicker = true }
                ) {
                    OutlinedTextField(
                        value = formatTime(endHour, endMinute),
                        onValueChange = {},
                        label = { Text("End Hour *") },
                        singleLine = true,
                        readOnly = true,
                        enabled = false,
                        isError = !isTimeIntervalValid,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Unspecified,
                            disabledBorderColor = if (!isTimeIntervalValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            Text("⌛", fontSize = 14.sp)
                        }
                    )
                }
            }

            // Inline Validation Warning
            if (!isTimeIntervalValid) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⚠️ End hour (${formatTime(endHour, endMinute)}) cannot be earlier than or equal to Start hour (${formatTime(startHour, startMinute)})",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Photo Attachment Section
            Text(
                text = "Attach Event Cover Photo",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray
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
                    Text("Photo attached!", fontSize = 12.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    TextButton(onClick = { photoUrl = null }) {
                        Text("Remove Photo", fontSize = 12.sp, color = Color.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
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
                        errorMessage = "End hour must be later than Start hour."
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val formattedDateTime = "$selectedDateText ${formatTime(startHour, startMinute)} - ${formatTime(endHour, endMinute)}"
                        onCreateEvent(title, description, selectedCategory, formattedDateTime, photoUrl)
                        onDismissRequest()
                    }
                },
                enabled = isTimeIntervalValid && title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = if (isEditing) "Save Changes" else "Publish Event on Map",
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
                    Text("Confirm Date")
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

                    // Auto-adjust end hour if start hour exceeds or equals end hour
                    val newStartTotal = startHour * 60 + startMinute
                    if (newStartTotal >= endTotalMinutes) {
                        endHour = (startHour + 1).coerceAtMost(23)
                        endMinute = startMinute
                    }

                    showStartTimePicker = false
                }) {
                    Text("Set Start Hour")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Start Hour", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                TimePicker(state = startTimePickerState)
            }
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
                    Text("Set End Hour")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select End Hour", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                TimePicker(state = endTimePickerState)
            }
        }
    }
}
