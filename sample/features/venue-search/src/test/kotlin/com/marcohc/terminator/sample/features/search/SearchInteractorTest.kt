package com.marcohc.terminator.sample.features.search

import com.marcohc.terminator.core.mvi.domain.MviBaseInteractor
import com.marcohc.terminator.core.mvi.test.MviInteractorTest
import com.marcohc.terminator.sample.data.model.Venue
import com.marcohc.terminator.sample.data.repositories.ConnectionManager
import com.marcohc.terminator.sample.data.repositories.LocationUnknownException
import com.marcohc.terminator.sample.features.search.adapter.VenueItem
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.internal.operators.observable.ObservableJust
import io.reactivex.internal.operators.single.SingleJust
import org.junit.Test
import org.mockito.Mock

internal class SearchInteractorTest : MviInteractorTest<SearchIntention, SearchResult, SearchState>() {

    @Mock
    lateinit var router: SearchRouter
    @Mock
    lateinit var analytics: SearchAnalytics
    @Mock
    lateinit var resourceProvider: SearchResourceProvider
    @Mock
    lateinit var connectionManager: ConnectionManager
    @Mock
    lateinit var getVenuesByCityUseCase: GetVenuesByCityUseCase

    override fun provideInteractor(): MviBaseInteractor<SearchIntention, SearchResult, SearchState> = SearchInteractor(
        getVenuesByCityUseCase,
        router,
        analytics,
        resourceProvider,
        connectionManager
    )

    @Test
    fun `when initial then return show toast`() {
        whenever(connectionManager.observeConnection()).thenReturn(ObservableJust(true))

        sendIntention(SearchIntention.Initial)

        testObserver.assertValueAt(1) { it.connected.consume()!! }
    }

    @Test
    fun `when search then track event`() {
        val city = "Amsterdam"
        whenever(getVenuesByCityUseCase.execute(city)).thenReturn(Single.never())

        sendIntention(SearchIntention.Search(city))

        verify(analytics).logSearchClick()
    }

    @Test
    fun `when search then return loading`() {
        val city = "Amsterdam"
        whenever(getVenuesByCityUseCase.execute(city)).thenReturn(Single.never())

        sendIntention(SearchIntention.Search(city))

        assertTypedStateAt<SearchState>(1) { loading }
    }

    @Test
    fun `when search with error should return general error text`() {
        val city = "Amsterdam"
        val text = "bla bla"
        whenever(getVenuesByCityUseCase.execute(city)).thenReturn(Single.error(NullPointerException()))
        whenever(resourceProvider.getGeneralErrorText()).thenReturn(text)

        sendIntention(SearchIntention.Search(city))

        testObserver.assertValueAt(2) { it.status == text }
    }

    @Test
    fun `when search with error should return location unknown text`() {
        val city = "Amsterdam"
        val text = "bla bla"
        whenever(getVenuesByCityUseCase.execute(city)).thenReturn(Single.error(LocationUnknownException))
        whenever(resourceProvider.getLocationUnknownText()).thenReturn(text)

        sendIntention(SearchIntention.Search(city))

        testObserver.assertValueAt(2) { it.status == text }
    }

    @Test
    fun `when search with success and empty items should return no items text`() {
        val noItemsText = "No places found in AmsterdamXXX"
        whenever(getVenuesByCityUseCase.execute(noItemsText)).thenReturn(SingleJust(emptyList()))
        whenever(resourceProvider.getNoItemsString(noItemsText)).thenReturn(noItemsText)

        sendIntention(SearchIntention.Search(noItemsText))

        assertTypedStateAt<SearchState>(2) { status == noItemsText}
    }

    @Test
    fun `when search with success and empty items should return items`() {
        val city = "Amsterdam"
        val venue = Venue()
        whenever(getVenuesByCityUseCase.execute(city)).thenReturn(SingleJust(listOf(venue)))

        sendIntention(SearchIntention.Search(city))

        testObserver.assertValueAt(2) { it.items.isNotEmpty() }
    }

    @Test
    fun `when item click then track event`() {
        val item = mock<VenueItem.Venue>()
        whenever(router.goToVenueDetails(item)).thenReturn(Completable.complete())

        sendIntention(SearchIntention.ItemClick(item))

        verify(analytics).logItemClick()
    }

    @Test
    fun `when item click then go to details`() {
        val item = mock<VenueItem.Venue>()

        sendIntention(SearchIntention.ItemClick(item))

        verify(router).goToVenueDetails(item)
    }

}
