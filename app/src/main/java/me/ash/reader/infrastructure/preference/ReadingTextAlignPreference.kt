package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKeys
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put

sealed class ReadingTextAlignPreference(val value: Int) : Preference() {
    object Left : ReadingTextAlignPreference(0)
    object Right : ReadingTextAlignPreference(1)
    object Center : ReadingTextAlignPreference(2)
    object Justify : ReadingTextAlignPreference(3)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(
                DataStoreKeys.ReadingTextAlign,
                value
            )
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            Left -> context.getString(R.string.align_left)
            Right -> context.getString(R.string.align_right)
            Center -> context.getString(R.string.center_text)
            Justify -> context.getString(R.string.justify)
        }

    fun toTextAlign(): TextAlign =
        when (this) {
            Left -> TextAlign.Start
            Right -> TextAlign.End
            Center -> TextAlign.Center
            Justify -> TextAlign.Justify
        }

    fun toTextAlignCSS(): String =
        when (this) {
            Left -> "left"
            Right -> "right"
            Center -> "center"
            Justify -> "justify"
        }

    fun toAlignment(): Alignment.Horizontal =
        when (this) {
            Left -> Alignment.Start
            Right -> Alignment.End
            Center -> Alignment.CenterHorizontally
            Justify -> Alignment.Start
        }

    companion object {

        val default = Left
        val values = listOf(Left, Right, Center, Justify)

        fun fromPreferences(preferences: Preferences): ReadingTextAlignPreference =
            when (preferences[DataStoreKeys.ReadingTextAlign.key]) {
                0 -> Left
                1 -> Right
                2 -> Center
                3 -> Justify
                else -> default
            }
    }
}
