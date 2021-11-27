@file:Suppress("unused")

package com.marcohc.terminator.core.preferences

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.CopyOnWriteArraySet

class AndroidPreferences(
    context: Context,
    preferencesName: String
) : Preferences {

    private val preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    private val listeners = CopyOnWriteArraySet<PreferencesChangeListener>()

    private val changeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        listeners.forEach { it.onPrefsValueChanged(this, key) }
    }

    init {
        preferences.registerOnSharedPreferenceChangeListener(changeListener)
    }

    override fun getString(key: String, defValue: String?): String? =
        preferences.getString(key, defValue)

    override fun putString(key: String, value: String?) {
        preferences.edit().putString(key, value).apply()
    }

    override fun getInt(key: String, defValue: Int): Int = preferences.getInt(key, defValue)
    override fun putInt(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    override fun getLong(key: String, defValue: Long): Long = preferences.getLong(key, defValue)
    override fun putLong(key: String, value: Long) {
        preferences.edit().putLong(key, value).apply()
    }

    override fun getFloat(key: String, defValue: Float): Float = preferences.getFloat(key, defValue)
    override fun putFloat(key: String, value: Float) {
        preferences.edit().putFloat(key, value).apply()
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean =
        preferences.getBoolean(key, defValue)

    override fun putBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    override fun batchEdit(actions: PrefsBatchEditor.() -> Unit) {
        preferences.edit()
            .apply {
                AndroidPrefsBatchEditor(this).actions()
            }
            .apply()
    }

    override fun contains(key: String): Boolean = preferences.contains(key)

    override fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }

    override fun clear() {
        preferences.edit().clear().apply()
    }

    override fun addChangeListener(listener: PreferencesChangeListener) {
        listeners.add(listener)
    }

    override fun removeChangeListener(listener: PreferencesChangeListener) {
        listeners.remove(listener)
    }
}

private class AndroidPrefsBatchEditor constructor(
    val editor: SharedPreferences.Editor
) : PrefsBatchEditor {

    override fun putString(key: String, value: String?) {
        editor.putString(key, value)
    }

    override fun putInt(key: String, value: Int) {
        editor.putInt(key, value)
    }

    override fun putLong(key: String, value: Long) {
        editor.putLong(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        editor.putFloat(key, value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value)
    }

    override fun remove(key: String) {
        editor.remove(key)
    }
}
