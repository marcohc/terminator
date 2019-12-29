package com.marcohc.terminator.sample.features.detail

import com.marcohc.terminator.core.mvi.domain.MviBaseInteractor
import com.marcohc.terminator.core.mvi.test.MviInteractorTest
import com.marcohc.terminator.sample.data.model.Venue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Test
import org.mockito.Mock

internal class DetailIntegrationTest : MviInteractorTest<DetailIntention, DetailAction, DetailState>() {

    @Mock
    lateinit var analytics: DetailAnalytics

    @Mock
    lateinit var getVenueByIdUseCase: GetVenueByIdUseCase

    @Mock
    lateinit var resourceProvider: DetailResourceProvider

    override fun provideInteractor(): MviBaseInteractor<DetailIntention, DetailAction, DetailState> = DetailInteractor(
        getVenueByIdUseCase,
        analytics,
        resourceProvider
    )

    @Test(expected = IllegalStateException::class)
    fun `given loading state when loading result then throw exception`() {
        assertException(DetailState.Loading, DetailAction.Load)
    }

    @Test(expected = IllegalStateException::class)
    fun `given data state when render result then throw exception`() {
        assertException(DetailState.Data(mock()), DetailAction.Render(mock()))
    }

    @Test(expected = IllegalStateException::class)
    fun `given data state when error result then throw exception`() {
        assertException(DetailState.Data(mock()), DetailAction.Error(""))
    }

    @Test
    fun `when initial then return loading`() {
        val venueId = "venueId"
        whenever(resourceProvider.getErrorMessage()).thenReturn("")
        whenever(getVenueByIdUseCase.execute(venueId)).thenReturn(Single.never())

        sendIntention(DetailIntention.Initial(venueId))

        DetailState.Loading.assertStateAt(0)
    }

    @Test
    fun `given error use case when initial then return error`() {
        val venueId = "venueId"
        val throwable = NullPointerException()
        whenever(resourceProvider.getErrorMessage()).thenReturn("")
        whenever(getVenueByIdUseCase.execute(venueId)).thenReturn(Single.error(throwable))

        sendIntention(DetailIntention.Initial(venueId))

        DetailState.Error("").assertStateAt(1)
    }

    @Test
    fun `given error use case when initial then return data`() {
        val venueId = "venueId"
        val venue = Venue()
        whenever(resourceProvider.getErrorMessage()).thenReturn("")
        whenever(getVenueByIdUseCase.execute(venueId)).thenReturn(Single.just(venue))

        sendIntention(DetailIntention.Initial(venueId))

        DetailState.Data(venue).assertStateAt(1)
    }

}
