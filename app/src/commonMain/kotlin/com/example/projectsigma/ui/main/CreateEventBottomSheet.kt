package com.example.projectsigma.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectsigma.model.EventCategory
import kotlin.math.round

private fun round4(value: Double): Double = round(value * 10000.0) / 10000.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventBottomSheet(
    location: Pair<Double, Double>,
    onDismissRequest: () -> Unit,
    onCreateEvent: (title: String, description: String, category: EventCategory, dateTime: String, photoUrl: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(EventCategory.MEETUP) }
    var dateTime by remember { mutableStateOf("Today at 19:00") }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateText by remember { mutableStateOf("Today") }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(initialHour = 19, initialMinute = 0)

    // Preset high-quality photos for fast attachment
    val presetPhotos = listOf(
        "🎉 Party" to "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
        "🍕 Food" to "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=600",
        "⚽ Sports" to "https://images.unsplash.com/photo-1517649763962-0c623266010b?w=600",
        "🎨 Art" to "https://images.unsplash.com/photo-1536924940846-227afb31e2a5?w=600",
        "👥 Meetup" to "https://images.unsplash.com/photo-1515187029135-18ee286d815b?w=600"
    )

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
                text = "📍 Create Event Pin",
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

            Spacer(modifier = Modifier.height(12.dp))

            // Date & Time Picker Field (Clickable)
            OutlinedTextField(
                value = dateTime,
                onValueChange = { dateTime = it },
                label = { Text("Date & Time (Tap to select) *") },
                singleLine = true,
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("📅 Pick", fontSize = 12.sp)
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Photo Attachment Section
            Text(
                text = "Attach Event Cover Photo",
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
                presetPhotos.forEach { (label, url) ->
                    val selected = photoUrl == url
                    FilterChip(
                        selected = selected,
                        onClick = { photoUrl = if (selected) null else url },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
            }

            if (photoUrl != null) {
                Spacer(modifier = Modifier.height(6.dp))
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
                    } else {
                        onCreateEvent(title, description, selectedCategory, dateTime, photoUrl)
                        onDismissRequest()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = "Publish Event on Map",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        selectedDateText = "Date selected"
                    }
                    showDatePicker = false
                    showTimePicker = true // Chain to TimePicker
                }) {
                    Text("Next: Pick Time")
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

    // Material 3 Time Picker Dialog
    if (showTimePicker) {
        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val hour = timePickerState.hour.toString().padStart(2, '0')
                    val min = timePickerState.minute.toString().padStart(2, '0')
                    dateTime = "$selectedDateText at $hour:$min"
                    showTimePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select Time", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                TimePicker(state = timePickerState)
            }
        }
    }
}
