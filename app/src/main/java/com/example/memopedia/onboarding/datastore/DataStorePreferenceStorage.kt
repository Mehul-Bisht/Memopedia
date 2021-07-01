package com.example.memopedia.onboarding.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.memopedia.onboarding.datastore.PreferenceStorage.PreferencesKey.PREF_ONBOARDED
import com.example.memopedia.onboarding.datastore.PreferenceStorage.PreferencesKey.PREF_TAGS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStorePreferenceStorage @Inject constructor(
    private val dataStore: DataStore<Preferences>
): PreferenceStorage {

    companion object {
        const val PREFS_NAME = "onboarding_prefs"
    }

    override suspend fun save(isOnboarded: Boolean) {
        dataStore.edit { settings ->
            settings[PREF_ONBOARDED] = isOnboarded
        }
    }

    override suspend fun saveTags(tags: Set<String>) {
        dataStore.edit { settings ->
            settings[PREF_TAGS] = tags
        }
    }

    override val isOnboarded: Flow<Boolean> =
        dataStore.data.map { it[PREF_ONBOARDED] ?: false }

    override val tags: Flow<Set<String>>
        get() = dataStore.data.map { it[PREF_TAGS] ?: setOf() }
}