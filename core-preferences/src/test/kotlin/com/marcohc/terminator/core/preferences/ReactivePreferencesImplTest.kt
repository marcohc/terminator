package com.marcohc.terminator.core.preferences

import com.gojuno.koptional.None
import com.gojuno.koptional.Some
import com.gojuno.koptional.toOptional
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableContainer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class ReactivePreferencesImplTest {

    private companion object {
        private const val TEST_KEY = "reactive_preferences_test_key"
    }

    private val preferences = InMemoryPreferences()
    private lateinit var disposables: CompositeDisposable

    @Before
    fun setUp() {
        disposables = CompositeDisposable()
    }

    @After
    fun tearDown() {
        preferences.clear()
        disposables.dispose()
    }

    @Test
    fun whenStringExistsThenReturnOptionalSomeWithThisString() {
        val value = "NaNaNaNaNaNa, TEST_KEY"
        preferences.putString(TEST_KEY, value)

        testValueAccess { getString(TEST_KEY) }
            .assertValue(value.toOptional())
    }

    @Test
    fun whenStringDoesNotExistThenReturnOptionalNone() {
        testValueAccess { getString(TEST_KEY) }
            .assertValue(None)
    }

    @Test
    fun whenSavingStringThenItShouldBeInPrefs() {
        testValueSaving { putString(TEST_KEY, "TEST_KEY") }
        assertEquals("TEST_KEY", preferences.getString(TEST_KEY))
    }

    @Test
    fun whenFirstSubscribeToStringKeyWithNoValueThenDefaultValueShouldBeReturnedImmediately() {
        ReactivePreferencesImpl(preferences)
            .observeString(TEST_KEY, "ANOTHER_TEST_KEY")
            .test()
            .assertNoErrors()
            .assertNotComplete()
            .assertValue(Some("ANOTHER_TEST_KEY"))
    }

    @Test
    fun whenFirstSubscribeToStringKeyWithValueThenThisValueShouldBeReturnedImmediately() {
        preferences.putString(TEST_KEY, "TEST_KEY")

        ReactivePreferencesImpl(preferences)
            .observeString(TEST_KEY, "ANOTHER_TEST_KEY")
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertNotComplete()
            .assertValue(Some("TEST_KEY"))
    }

    @Test
    fun whenStringKeyValueChangesThenItShouldBeDelivered() {
        preferences.putString(TEST_KEY, "TEST_KEY")

        val test = ReactivePreferencesImpl(preferences)
            .observeString(TEST_KEY, null)
            .test()
            .addTo(disposables)

        preferences.putString(TEST_KEY, "ANOTHER_TEST_KEY")
        preferences.remove(TEST_KEY)

        test
            .assertNoErrors()
            .assertNotComplete()
            .assertValues(Some("TEST_KEY"), Some("ANOTHER_TEST_KEY"), None)
    }

    @Test
    fun whenStringKeyObserverIsDisposedThenThePrefsChangeListenerShouldBeRemoved() {
        val disposable = ReactivePreferencesImpl(preferences)
            .observeString(TEST_KEY, "VALUE")
            .test()

        assertEquals(1, preferences.listenersCount)

        preferences.putString(TEST_KEY, "TEST_KEY")
        preferences.remove(TEST_KEY)

        disposable.dispose()
        assertEquals(0, preferences.listenersCount)
    }

    @Test
    fun whenIntExistsThenReturnIt() {
        preferences.putInt(TEST_KEY, Int.MAX_VALUE)

        testValueAccess { getInt(TEST_KEY, Int.MIN_VALUE) }
            .assertValue(Int.MAX_VALUE)
    }

    @Test
    fun whenIntDoesNotExistThenReturnDefaultValue() {
        testValueAccess { getInt(TEST_KEY, Int.MIN_VALUE) }
            .assertValue(Int.MIN_VALUE)
    }

    @Test
    fun whenSavingIntThenItShouldBeInPrefs() {
        testValueSaving { putInt(TEST_KEY, 42) }
        assertEquals(42, preferences.getInt(TEST_KEY, Int.MIN_VALUE))
    }

    @Test
    fun whenFirstSubscribeToIntKeyWithNoValueThenDefaultValueShouldBeReturnedImmediately() {
        ReactivePreferencesImpl(preferences)
            .observeInt(TEST_KEY, 42)
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertNotComplete()
            .assertValue(42)
    }

    @Test
    fun whenFirstSubscribeToIntKeyWithValueThenThisValueShouldBeReturnedImmediately() {
        preferences.putInt(TEST_KEY, 42)

        ReactivePreferencesImpl(preferences)
            .observeInt(TEST_KEY, 42)
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertNotComplete()
            .assertValue(42)
    }

    @Test
    fun whenIntKeyValueChangesThenItShouldBeDelivered() {
        preferences.putInt(TEST_KEY, 42)

        val test = ReactivePreferencesImpl(preferences)
            .observeInt(TEST_KEY, -1)
            .test()
            .addTo(disposables)

        preferences.putInt(TEST_KEY, 24)
        preferences.remove(TEST_KEY)

        test
            .assertNoErrors()
            .assertNotComplete()
            .assertValues(42, 24, -1)
    }

    @Test
    fun whenIntKeyObserverIsDisposedThenThePrefsChangeListenerShouldBeRemoved() {
        val disposable = ReactivePreferencesImpl(preferences)
            .observeInt(TEST_KEY, 42)
            .test()

        assertEquals(1, preferences.listenersCount)

        preferences.putInt(TEST_KEY, 24)
        preferences.remove(TEST_KEY)

        disposable.dispose()
        assertEquals(0, preferences.listenersCount)
    }

    @Test
    fun whenLongExistsThenReturnIt() {
        preferences.putLong(TEST_KEY, Long.MAX_VALUE)

        testValueAccess { getLong(TEST_KEY, Long.MIN_VALUE) }
            .assertValue(Long.MAX_VALUE)
    }

    @Test
    fun whenLongDoesNotExistThenReturnDefaultValue() {
        testValueAccess { getLong(TEST_KEY, Long.MIN_VALUE) }
            .assertValue(Long.MIN_VALUE)
    }

    @Test
    fun whenSavingLongThenItShouldBeInPrefs() {
        testValueSaving { putLong(TEST_KEY, 42L) }
        assertEquals(42L, preferences.getLong(TEST_KEY, Long.MIN_VALUE))
    }

    @Test
    fun whenFirstSubscribeToLongKeyWithNoValueThenDefaultValueShouldBeReturnedImmediately() {
        ReactivePreferencesImpl(preferences)
            .observeLong(TEST_KEY, 42L)
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertNotComplete()
            .assertValue(42L)
    }

    @Test
    fun whenFirstSubscribeToLongKeyWithValueThenThisValueShouldBeReturnedImmediately() {
        preferences.putLong(TEST_KEY, 42L)

        ReactivePreferencesImpl(preferences)
            .observeLong(TEST_KEY, 42L)
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertNotComplete()
            .assertValue(42L)
    }

    @Test
    fun whenLongKeyValueChangesThenItShouldBeDelivered() {
        preferences.putLong(TEST_KEY, 42L)

        val test = ReactivePreferencesImpl(preferences)
            .observeLong(TEST_KEY, -1L)
            .test()
            .addTo(disposables)

        preferences.putLong(TEST_KEY, 24L)
        preferences.remove(TEST_KEY)

        test
            .assertNoErrors()
            .assertNotComplete()
            .assertValues(42L, 24L, -1L)
    }

    @Test
    fun whenLongKeyObserverIsDisposedThenThePrefsChangeListenerShouldBeRemoved() {
        val disposable = ReactivePreferencesImpl(preferences)
            .observeLong(TEST_KEY, 42L)
            .test()

        assertEquals(1, preferences.listenersCount)

        preferences.putLong(TEST_KEY, 24L)
        preferences.remove(TEST_KEY)

        disposable.dispose()
        assertEquals(0, preferences.listenersCount)
    }

    @Test
    fun whenFloatExistsThenReturnIt() {
        preferences.putFloat(TEST_KEY, Float.MAX_VALUE)

        testValueAccess { getFloat(TEST_KEY, Float.MIN_VALUE) }
            .assertValue(Float.MAX_VALUE)
    }

    @Test
    fun whenFloatDoesNotExistThenReturnDefaultValue() {
        testValueAccess { getFloat(TEST_KEY, Float.MIN_VALUE) }
            .assertValue(Float.MIN_VALUE)
    }

    @Test
    fun whenSavingFloatThenItShouldBeInPrefs() {
        testValueSaving { putFloat(TEST_KEY, 42F) }
        assertEquals(42F, preferences.getFloat(TEST_KEY, Float.MIN_VALUE))
    }

    @Test
    fun whenFirstSubscribeToFloatKeyWithNoValueThenDefaultValueShouldBeReturnedImmediately() {
        ReactivePreferencesImpl(preferences)
            .observeFloat(TEST_KEY, 42F)
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertNotComplete()
            .assertValue(42F)
    }

    @Test
    fun whenFirstSubscribeToFloatKeyWithValueThenThisValueShouldBeReturnedImmediately() {
        preferences.putFloat(TEST_KEY, 42F)

        ReactivePreferencesImpl(preferences)
            .observeFloat(TEST_KEY, 42F)
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertNotComplete()
            .assertValue(42F)
    }

    @Test
    fun whenFloatKeyValueChangesThenItShouldBeDelivered() {
        preferences.putFloat(TEST_KEY, 42F)

        val test = ReactivePreferencesImpl(preferences)
            .observeFloat(TEST_KEY, -1F)
            .test()
            .addTo(disposables)

        preferences.putFloat(TEST_KEY, 24F)
        preferences.remove(TEST_KEY)

        test
            .assertNoErrors()
            .assertNotComplete()
            .assertValues(42F, 24F, -1F)
    }

    @Test
    fun whenFloatKeyObserverIsDisposedThenThePrefsChangeListenerShouldBeRemoved() {
        val disposable = ReactivePreferencesImpl(preferences)
            .observeFloat(TEST_KEY, 42F)
            .test()

        assertEquals(1, preferences.listenersCount)

        preferences.putFloat(TEST_KEY, 24F)
        preferences.remove(TEST_KEY)

        disposable.dispose()
        assertEquals(0, preferences.listenersCount)
    }



    @Test
    fun whenBooleanExistsThenReturnIt() {
        preferences.putBoolean(TEST_KEY, true)

        testValueAccess { getBoolean(TEST_KEY, false) }
            .assertValue(true)
    }

    @Test
    fun whenBooleanDoesNotExistThenReturnDefaultValue() {
        testValueAccess { getBoolean(TEST_KEY, true) }
            .assertValue(true)
    }

    @Test
    fun whenSavingBooleanThenItShouldBeInPrefs() {
        testValueSaving { putBoolean(TEST_KEY, true) }
        assertEquals(true, preferences.getBoolean(TEST_KEY, false))
    }

    @Test
    fun whenFirstSubscribeToBooleanKeyWithNoValueThenDefaultValueShouldBeReturnedImmediately() {
        ReactivePreferencesImpl(preferences)
            .observeBoolean(TEST_KEY, true)
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertNotComplete()
            .assertValue(true)
    }

    @Test
    fun whenFirstSubscribeToBooleanKeyWithValueThenThisValueShouldBeReturnedImmediately() {
        preferences.putBoolean(TEST_KEY, true)

        ReactivePreferencesImpl(preferences)
            .observeBoolean(TEST_KEY, false)
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertNotComplete()
            .assertValue(true)
    }

    @Test
    fun whenBooleanKeyValueChangesThenItShouldBeDelivered() {
        preferences.putBoolean(TEST_KEY, true)

        val test = ReactivePreferencesImpl(preferences)
            .observeBoolean(TEST_KEY, true)
            .test()
            .addTo(disposables)

        preferences.putBoolean(TEST_KEY, false)
        preferences.remove(TEST_KEY)

        test
            .assertNoErrors()
            .assertNotComplete()
            .assertValues(true, false, true)
    }

    @Test
    fun whenBooleanKeyObserverIsDisposedThenThePrefsChangeListenerShouldBeRemoved() {
        val disposable = ReactivePreferencesImpl(preferences)
            .observeBoolean(TEST_KEY, true)
            .test()

        assertEquals(1, preferences.listenersCount)

        preferences.putBoolean(TEST_KEY, true)
        preferences.remove(TEST_KEY)

        disposable.dispose()
        assertEquals(0, preferences.listenersCount)
    }



    @Test
    fun whenValueDoesNotExistsForTheKeyThenReturnFalseForContainsCall() {
        ReactivePreferencesImpl(preferences)
            .contains(TEST_KEY)
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertValue(false)
    }

    @Test
    fun whenValueExistsForTheKeyThenReturnTrueForContainsCall() {
        preferences.putInt(TEST_KEY, 42)

        ReactivePreferencesImpl(preferences)
            .contains(TEST_KEY)
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertValue(true)
    }

    @Test
    fun whenBatchEditingOccursThenValuesShouldBeProperlyStored() {
        preferences.putString("ZERO", "ANOTHER_TEST_KEY")

        ReactivePreferencesImpl(preferences)
            .batchEdit {
                remove("ZERO")
                putString("ONE", "TEST_KEY")
                putInt("TWO", 42)
                putLong("THREE", 42L)
                putFloat("FOUR", 42F)
                putBoolean("FIVE", true)
            }
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertNoValues()

        assertEquals(5, preferences.itemsCount)
        assertEquals("TEST_KEY", preferences.getString("ONE"))
        assertEquals(42, preferences.getInt("TWO", Int.MIN_VALUE))
        assertEquals(42L, preferences.getLong("THREE", Long.MIN_VALUE))
        assertEquals(42F, preferences.getFloat("FOUR", Float.NaN))
        assertEquals(true, preferences.getBoolean("FIVE", false))
        assertFalse(preferences.contains("ZERO"))
    }

    @Test
    fun whenRemovingKeyThenItShouldBeRemoved() {
        preferences.putInt(TEST_KEY, 42)

        ReactivePreferencesImpl(preferences)
            .remove(TEST_KEY)
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertNoValues()
            .assertComplete()

        assertEquals(false, TEST_KEY in preferences)
    }

    @Test
    fun whenRemovingNonExistingKeyThenNoErrorsShouldHappen() {
        ReactivePreferencesImpl(preferences)
            .remove(TEST_KEY)
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertNoValues()
            .assertComplete()

        assertEquals(false, TEST_KEY in preferences)
    }

    @Test
    fun whenClearingEverythingThenShouldBeNoDataLeft() {
        preferences.putString("0", "TEST_KEY")
        preferences.putInt("1", 42)
        preferences.putLong("2", 42L)
        preferences.putFloat("3", 42F)
        preferences.putBoolean("4", true)

        assertEquals(5, preferences.itemsCount)

        ReactivePreferencesImpl(preferences)
            .clear()
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertNoValues()
            .assertComplete()

        assertEquals(0, preferences.itemsCount)
    }

    @Test
    fun whenClearingEverythingWhenNoDataExistsThenNoErrorShouldHappen() {
        ReactivePreferencesImpl(preferences)
            .clear()
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertNoValues()
            .assertComplete()

        assertEquals(0, preferences.itemsCount)
    }

    private fun <Type> testValueAccess(retriever: ReactivePreferences.() -> Single<Type>) =
        ReactivePreferencesImpl(preferences)
            .retriever()
            .test()
            .addTo(disposables)
            .assertNoErrors()

    private fun testValueSaving(saver: ReactivePreferences.() -> Completable) {
        ReactivePreferencesImpl(preferences)
            .saver()
            .test()
            .addTo(disposables)
            .assertNoErrors()
            .assertNoValues()
            .assertComplete()
    }

    private fun <T : Disposable> T.addTo(container: DisposableContainer): T = apply {
        container.add(this)
    }
}
