package com.startupapps.notescompose.feature.settings.data

interface SettingsRepository {
    fun getSettings(): AppSettings
    fun setGridLayout(value: Boolean)
    fun setFontSize(value: Float)
}
