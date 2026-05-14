package com.startupapps.notescompose.feature.settings.data

import android.content.SharedPreferences

class SharedPreferencesSettingsRepository(
    private val preferences: SharedPreferences
) : SettingsRepository {

    override fun getSettings(): AppSettings =
        AppSettings(
            isGridLayout = preferences.getBoolean(KEY_GRID_LAYOUT, true),
            fontSize = preferences.getFloat(KEY_FONT_SIZE, 16f)
        )

    override fun setGridLayout(value: Boolean) {
        preferences.edit().putBoolean(KEY_GRID_LAYOUT, value).apply()
    }

    override fun setFontSize(value: Float) {
        preferences.edit().putFloat(KEY_FONT_SIZE, value).apply()
    }

    private companion object {
        const val KEY_GRID_LAYOUT = "is_grid_layout"
        const val KEY_FONT_SIZE = "font_size"
    }
}
