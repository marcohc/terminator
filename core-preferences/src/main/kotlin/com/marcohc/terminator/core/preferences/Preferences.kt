package com.marcohc.terminator.core.preferences

interface Preferences {
    fun getString(key: String, defValue: String? = null): String?
    fun putString(key: String, value: String?)

    fun getInt(key: String, defValue: Int): Int
    fun putInt(key: String, value: Int)

    fun getLong(key: String, defValue: Long): Long
    fun putLong(key: String, value: Long)

    fun getFloat(key: String, defValue: Float): Float
    fun putFloat(key: String, value: Float)

    fun getBoolean(key: String, defValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)

    fun batchEdit(actions: PrefsBatchEditor.() -> Unit)

    operator fun contains(key: String): Boolean
    fun remove(key: String)
    fun clear()

    fun addChangeListener(listener: PreferencesChangeListener)
    fun removeChangeListener(listener: PreferencesChangeListener)
}
