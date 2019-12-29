package com.marcohc.terminator.sample.features.search

import com.marcohc.terminator.sample.data.model.Venue
import com.marcohc.terminator.sample.data.repositories.ConnectionManager
import com.marcohc.terminator.sample.data.repositories.VenueRepository
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.internal.operators.single.SingleJust
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

internal class GetVenuesByCityUseCaseTest {

    @Mock
    lateinit var connectionManager: ConnectionManager
    @Mock
    private lateinit var venueRepository: VenueRepository

    private lateinit var useCase: GetVenuesByCityUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        useCase = GetVenuesByCityUseCase(
            connectionManager,
            venueRepository,
            Schedulers.trampoline()
        )
    }

    @Test
    fun `given connection when use case executes then return items`() {
        val city = "Madrid"
        val items = emptyList<Venue>()
        whenever(connectionManager.isConnected()).thenReturn(true)
        whenever(venueRepository.getFromNetwork(city)).thenReturn(SingleJust(items))

        useCase.execute(city)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue { it == items }
    }

    @Test
    fun `given connection when use case executes then save items`() {
        val city = "Madrid"
        val items = emptyList<Venue>()
        whenever(connectionManager.isConnected()).thenReturn(true)
        whenever(venueRepository.getFromNetwork(city)).thenReturn(SingleJust(items))

        useCase.execute(city).test()

        verify(venueRepository).saveAll(items)
    }

    @Test
    fun `given no connection when use case executes then return local items`() {
        val city = "Madrid"
        val items = emptyList<Venue>()
        whenever(connectionManager.isConnected()).thenReturn(false)
        whenever(venueRepository.getFromLocal(city)).thenReturn(SingleJust(items))

        useCase.execute(city)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue { it == items }
    }

}
