package com.pedro.octoos.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/** Paletas de destaque escolhidas em Configurações. */
enum class OctoAccent(val key: String, val label: String, val color: Color) {
    LIME("LIME", "LIMA", Color(0xFFD4FF3A)),
    CYAN("CYAN", "CIANO", Color(0xFF38F0FF)),
    MAGENTA("MAGENTA", "MAGENTA", Color(0xFFFF3AD4)),
    AMBER("AMBER", "ÂMBAR", Color(0xFFFFB13A)),
    RED("RED", "VERMELHO", Color(0xFFFF3A3A));

    companion object {
        fun fromKey(key: String?): OctoAccent = entries.firstOrNull { it.key == key } ?: LIME
    }
}

/** Cor de destaque atual. Ao trocar, toda tela que lê OctoLime recompõe. */
object OctoPalette {
    var accent: OctoAccent by mutableStateOf(OctoAccent.LIME)
}

// Cyber brutal strict color palette
val OctoBackground = Color(0xFF0A0A0A)
val OctoText = Color(0xFFF4F4F4)
/** Destaque principal: segue a paleta escolhida em Configurações. */
val OctoLime: Color
    get() = OctoPalette.accent.color
val OctoTile = Color(0xFF161616)
val OctoBorder = Color(0x1FFFFFFF) // #FFFFFF1F
val OctoGray = Color(0xFF8E8E9C)
val OctoCyan = Color(0xFF00B8D9)
val OctoDanger = Color(0xFFFF3A3A)
