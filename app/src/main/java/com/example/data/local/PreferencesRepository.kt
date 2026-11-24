package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.ledgerDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "daily_ledger_prefs",
)

data class AppPreferences(
    val scriptUrl: String = "",
    val autoSync: Boolean = false,
    val lastSyncedAt: Long = 0L,
    val hasPendingChanges: Boolean = false,
    val currencySymbol: String = "৳",
    val languageCode: String = "bn",
    val themeMode: String = "system",
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val SCRIPT_URL = stringPreferencesKey("apps_script_url")
        val AUTO_SYNC = booleanPreferencesKey("auto_sync")
        val LAST_SYNCED_AT = longPreferencesKey("last_synced_at")
        val HAS_PENDING_CHANGES = booleanPreferencesKey("has_pending_changes")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val preferencesFlow: Flow<AppPreferences> = context.ledgerDataStore.data.map { prefs ->
        AppPreferences(
            scriptUrl = prefs[Keys.SCRIPT_URL].orEmpty(),
            autoSync = prefs[Keys.AUTO_SYNC] ?: false,
            lastSyncedAt = prefs[Keys.LAST_SYNCED_AT] ?: 0L,
            hasPendingChanges = prefs[Keys.HAS_PENDING_CHANGES] ?: false,
            currencySymbol = prefs[Keys.CURRENCY_SYMBOL] ?: "৳",
            languageCode = prefs[Keys.LANGUAGE_CODE] ?: "bn",
            themeMode = prefs[Keys.THEME_MODE] ?: "system",
        )
    }

    suspend fun getPreferencesOnce(): AppPreferences = preferencesFlow.first()

    suspend fun saveScriptUrl(url: String) {
        context.ledgerDataStore.edit { it[Keys.SCRIPT_URL] = url.trim() }
    }

    suspend fun setAutoSync(enabled: Boolean) {
        context.ledgerDataStore.edit { it[Keys.AUTO_SYNC] = enabled }
    }

    suspend fun markSynced(timestampMs: Long = System.currentTimeMillis()) {
        context.ledgerDataStore.edit {
            it[Keys.LAST_SYNCED_AT] = timestampMs
            it[Keys.HAS_PENDING_CHANGES] = false
        }
    }

    suspend fun markPendingChanges() {
        context.ledgerDataStore.edit { it[Keys.HAS_PENDING_CHANGES] = true }
    }

    suspend fun setCurrency(symbol: String) {
        context.ledgerDataStore.edit { it[Keys.CURRENCY_SYMBOL] = symbol }
    }

    suspend fun setLanguage(code: String) {
        context.ledgerDataStore.edit { it[Keys.LANGUAGE_CODE] = code }
    }

    suspend fun setThemeMode(mode: String) {
        context.ledgerDataStore.edit { it[Keys.THEME_MODE] = mode }
    }
}
