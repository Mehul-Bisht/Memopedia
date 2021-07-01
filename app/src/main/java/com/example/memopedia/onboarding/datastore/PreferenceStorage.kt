package com.example.memopedia.onboarding.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow

interface PreferenceStorage {

    suspend fun save(isOnboarded: Boolean)
    suspend fun saveTags(tags: Set<String>)
    val isOnboarded: Flow<Boolean>
    val tags: Flow<Set<String>>

    object PreferencesKey{

        val PREF_ONBOARDED = booleanPreferencesKey("onboarded")
        val PREF_TAGS = stringSetPreferencesKey("tags")
    }
}