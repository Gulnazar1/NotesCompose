package com.startupapps.notescompose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    onDismiss: () -> Unit,
    isGridLayout: Boolean,
    onToggleLayout: () -> Unit,
    fontSize: Float,
    onChangeFontSize: (Float) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "Настройки",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text("Макет", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Grid Button (AccountBox as Grid icon replacement)
                IconButton(
                    onClick = { if (!isGridLayout) onToggleLayout() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isGridLayout) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (isGridLayout) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(Icons.Default.AccountBox, contentDescription = "Grid")
                }


                IconButton(
                    onClick = { if (isGridLayout) onToggleLayout() },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (!isGridLayout) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (!isGridLayout) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "List")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Размер шрифта", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { if (fontSize > 12f) onChangeFontSize(fontSize - 2f) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Decrease")
                }

                Text(
                    text = "${fontSize.toInt()} sp",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )

                IconButton(onClick = { if (fontSize < 32f) onChangeFontSize(fontSize + 2f) }) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }
        }
    }
}
