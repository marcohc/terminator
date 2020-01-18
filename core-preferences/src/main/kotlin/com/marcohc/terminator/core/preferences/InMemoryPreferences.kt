package com.marcohc.terminator.core.preferences

import java.util.concurrent.CopyOnWriteArraySet

class InMemoryPreferences : Preferences {

    private val preferencesMap = HashMap<String, Any?>()

    private val listeners = CopyOnWriteArraySet<PreferencesChangeListener>()

    val itemsCount get() = preferencesMap.size
    val listenersCount get() = listeners.size

    override fun getString(key: String, defValue: String?): String? = preferencesMap.getOrElse(key) { defValue } as String?
    override fun putString(key: String, value: String?) {
        preferencesMap[key] = value
        notifyListeners(key)
    }

    override fun getInt(key: String, defValue: Int): Int = preferencesMap.getOrElse(key) { defValue } as Int
    override fun putInt(key: String, value: Int) {
        preferencesMap[key] = value
        notifyListeners(key)
    }

    override fun getLong(key: String, defValue: Long): Long = preferencesMap.getOrElse(key) { defValue } as Long
    override fun putLong(key: String, value: Long) {
        preferencesMap[key] = value
        notifyListeners(key)
    }

    override fun getFloat(key: String, defValue: Float): Float = preferencesMap.getOrElse(key) { defValue } as Float
    override fun putFloat(key: String, value: Float) {
        preferencesMap[key] = value
        notifyListeners(key)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean = preferencesMap.getOrElse(key) { defValue } as Boolean
    override fun putBoolean(key: String, value: Boolean) {
        preferencesMap[key] = value
        notifyListeners(key)
    }

    override fun batchEdit(actions: PrefsBatchEditor.() -> Unit) {
        InMemoryPrefsBatchEditor(this, preferencesMap, listeners).actions()
    }

    override fun contains(key: String): Boolean = key in preferencesMap

    override fun remove(key: String) {
        preferencesMap -= key
        notifyListeners(key)
    }

    override fun clear() {
        preferencesMap.clear()
    }

    override fun addChangeListener(listener: PreferencesChangeListener) {
        listeners += listener
    }

    override fun removeChangeListener(listener: PreferencesChangeListener) {
        listeners -= listener
    }

    private fun notifyListeners(key: String) {
        listeners.forEach { it.onPrefsValueChanged(this, key) }
    }
}

private class InMemoryPrefsBatchEditor constructor(
        private val preferences: Preferences,
        private val map: MutableMap<String, Any?>,
        private val listeners: Set<PreferencesChangeListener>
) : PrefsBatchEditor {

    override fun putString(key: String, value: String?) {
        map[key] = value
        notifyListeners(key)
    }

    override fun putInt(key: String, value: Int) {
        map[key] = value
        notifyListeners(key)
    }

    override fun putLong(key: String, value: Long) {
        map[key] = value
        notifyListeners(key)
    }

    override fun putFloat(key: String, value: Float) {
        map[key] = value
        notifyListeners(key)
    }

    override fun putBoolean(key: String, value: Boolean) {
        map[key] = value
        notifyListeners(key)
    }

    override fun remove(key: String) {
        map -= key
        notifyListeners(key)
    }

    private fun notifyListeners(key: String) {
        listeners.forEach { it.onPrefsValueChanged(preferences, key) }
    }
}
