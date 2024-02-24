package com.marcohc.terminator.core.preferences

interface PreferencesChangeListener {
    fun onPrefsValueChanged(preferences: Preferences, key: String?)
}
