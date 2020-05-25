package com.marcohc.terminator.core.mvi.ext

/**
 * Class to keep the count of shared modules and shared scopes
 */
class ScopeCounter {

    private val parentScopes = mutableListOf<String>()

    fun getOnTop() = parentScopes.last()

    fun add(scopeId: String) = parentScopes.add(scopeId)

    fun remove(scopeId: String) = parentScopes.remove(scopeId)

    fun isEmpty() = parentScopes.isEmpty()

    override fun toString() = "ScopeCounter(parentScopes=$parentScopes)"
}
