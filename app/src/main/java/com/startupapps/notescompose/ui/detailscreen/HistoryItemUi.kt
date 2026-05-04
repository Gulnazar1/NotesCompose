package com.startupapps.notescompose.ui.detailscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.startupapps.notescompose.domain.model.NoteHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryItemUI(version: NoteHistory, onRestore: (NoteHistory) -> Unit) {
    val date = Date(version.timestamp)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    Surface(
        onClick = { onRestore(version) },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(timeFormat.format(date), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text(dateFormat.format(date), style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(version.title.ifBlank { "Без заголовка" }, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                Text(version.text.ifBlank { "Пустой текст" }, style = MaterialTheme.typography.bodyMedium, maxLines = 1, color = Color.Gray)
            }
            Icon(Icons.Default.Restore, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        }
    }
}
