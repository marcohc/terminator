package com.marcohc.terminator.core.preferences

import com.gojuno.koptional.Optional
import com.gojuno.koptional.toOptional
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import java.util.concurrent.atomic.AtomicBoolean

interface ReactivePreferences {

    fun getString(key: String, defValue: String? = null): Single<Optional<String>>
    fun putString(key: String, value: String?): Completable

    fun getInt(key: String, defValue: Int): Single<Int>
    fun putInt(key: String, value: Int): Completable

    fun getLong(key: String, defValue: Long): Single<Long>
    fun putLong(key: String, value: Long): Completable

    fun getFloat(key: String, defValue: Float): Single<Float>
    fun putFloat(key: String, value: Float): Completable

    fun getBoolean(key: String, defValue: Boolean): Single<Boolean>
    fun putBoolean(key: String, value: Boolean): Completable

    fun batchEdit(actions: PrefsBatchEditor.() -> Unit): Completable

    fun contains(key: String): Single<Boolean>
    fun remove(key: String): Completable
    fun clear(): Completable

    @Deprecated("Do not use, contains a tricky bug when obfuscation")
    fun observeString(key: String, defValue: String?): Observable<Optional<String>>
    fun observeInt(key: String, defValue: Int): Observable<Int>
    fun observeLong(key: String, defValue: Long): Observable<Long>
    fun observeFloat(key: String, defValue: Float): Observable<Float>
    fun observeBoolean(key: String, defValue: Boolean): Observable<Boolean>
}

@Suppress("NAME_SHADOWING")
class ReactivePreferencesImpl(
    private val preferences: Preferences
) : ReactivePreferences {

    override fun getString(key: String, defValue: String?): Single<Optional<String>> =
        Single.fromCallable { preferences.getString(key, defValue).toOptional() }

    override fun putString(key: String, value: String?): Completable =
        Completable.fromAction { preferences.putString(key, value) }

    override fun getInt(key: String, defValue: Int): Single<Int> =
        Single.fromCallable { preferences.getInt(key, defValue) }

    override fun putInt(key: String, value: Int): Completable =
        Completable.fromAction { preferences.putInt(key, value) }

    override fun getLong(key: String, defValue: Long): Single<Long> =
        Single.fromCallable { preferences.getLong(key, defValue) }

    override fun putLong(key: String, value: Long): Completable =
        Completable.fromAction { preferences.putLong(key, value) }

    override fun getFloat(key: String, defValue: Float): Single<Float> =
        Single.fromCallable { preferences.getFloat(key, defValue) }

    override fun putFloat(key: String, value: Float): Completable =
        Completable.fromAction { preferences.putFloat(key, value) }

    override fun getBoolean(key: String, defValue: Boolean): Single<Boolean> =
        Single.fromCallable { preferences.getBoolean(key, defValue) }

    override fun putBoolean(key: String, value: Boolean): Completable =
        Completable.fromAction { preferences.putBoolean(key, value) }

    override fun batchEdit(actions: PrefsBatchEditor.() -> Unit): Completable =
        Completable.fromAction { preferences.batchEdit(actions) }

    override fun contains(key: String): Single<Boolean> =
        Single.fromCallable { preferences.contains(key) }

    override fun remove(key: String): Completable =
        Completable.fromAction { preferences.remove(key) }

    override fun clear(): Completable =
        Completable.fromAction { preferences.clear() }

    override fun observeString(key: String, defValue: String?): Observable<Optional<String>> =
        PreferencesStringObservable(preferences, key, defValue)

    override fun observeInt(key: String, defValue: Int): Observable<Int> =
        PreferencesObservable(preferences, key, defValue) { key, defValue -> getInt(key, defValue) }

    override fun observeLong(key: String, defValue: Long): Observable<Long> =
        PreferencesObservable(preferences, key, defValue) { key, defValue ->
            getLong(
                key,
                defValue
            )
        }

    override fun observeFloat(key: String, defValue: Float): Observable<Float> =
        PreferencesObservable(preferences, key, defValue) { key, defValue ->
            getFloat(
                key,
                defValue
            )
        }

    override fun observeBoolean(key: String, defValue: Boolean): Observable<Boolean> =
        PreferencesObservable(preferences, key, defValue) { key, defValue ->
            getBoolean(
                key,
                defValue
            )
        }
}

private class PreferencesStringObservable(
    private val preferences: Preferences,
    private val prefKey: String,
    private val defValue: String?
) : Observable<Optional<String>>() {

    override fun subscribeActual(observer: Observer<in Optional<String>>) {
        val listener = PreferencesStringObservableListener(preferences, prefKey, defValue, observer)
        observer.onSubscribe(listener)
        preferences.addChangeListener(listener)
        observer.onNext(preferences.getString(prefKey, defValue).toOptional())
    }
}

private class PreferencesStringObservableListener(
    private val preferences: Preferences,
    private val prefKey: String,
    private val defValue: String?,
    private val observer: Observer<in Optional<String>>
) : SimpleDisposable(),
    PreferencesChangeListener {

    override fun onPrefsValueChanged(preferences: Preferences, key: String?) {
        if (!isDisposed && prefKey == key) {
            observer.onNext(preferences.getString(prefKey, defValue).toOptional())
        }
    }

    override fun onDispose() {
        preferences.removeChangeListener(this)
    }
}

private class PreferencesObservable<Type>(
    private val preferences: Preferences,
    private val prefKey: String,
    private val defValue: Type,
    private val retriever: Preferences.(String, Type) -> Type
) : Observable<Type>() {

    override fun subscribeActual(observer: Observer<in Type>) {
        val listener =
            PreferencesObservableListener(preferences, prefKey, defValue, observer, retriever)
        observer.onSubscribe(listener)
        preferences.addChangeListener(listener)
        observer.onNext(preferences.retriever(prefKey, defValue))
    }
}

private class PreferencesObservableListener<Type>(
    private val preferences: Preferences,
    private val key: String,
    private val defValue: Type,
    private val observer: Observer<in Type>,
    private val retriever: Preferences.(String, Type) -> Type
) : SimpleDisposable(),
    PreferencesChangeListener {

    override fun onPrefsValueChanged(preferences: Preferences, key: String?) {
        if (!isDisposed && this.key == key) {
            observer.onNext(preferences.retriever(this.key, defValue))
        }
    }

    override fun onDispose() {
        preferences.removeChangeListener(this)
    }
}

private abstract class SimpleDisposable : Disposable {

    private val disposed = AtomicBoolean(false)

    final override fun isDisposed(): Boolean = disposed.get()

    final override fun dispose() {
        if (disposed.compareAndSet(false, true)) {
            onDispose()
        }
    }

    protected abstract fun onDispose()
}
