package com.startupapps.notescompose.ui.detailscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.startupapps.notescompose.data.NoteHistoryEntity

@Composable
fun HistoryContent(history: List<NoteHistoryEntity>, onRestore: (NoteHistoryEntity) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
        Text("История изменений", modifier = Modifier.padding(24.dp), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                Text("Версий пока нет", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(history) { version ->
                    HistoryItemUI(version, onRestore)
                }
            }
        }
    }
}