package com.startupapps.notescompose.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable
    data object Main : Screen()

    @Serializable
    data class Trash(val isNotes: Boolean) : Screen()

    @Serializable
    data class Edit(val id: Int?) : Screen()

    @Serializable
    data class Detail(val id: Int) : Screen()
}