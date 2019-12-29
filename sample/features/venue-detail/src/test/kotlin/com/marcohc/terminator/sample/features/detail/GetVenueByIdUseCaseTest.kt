package com.marcohc.terminator.sample.features.detail

import com.marcohc.terminator.sample.data.model.Venue
import com.marcohc.terminator.sample.data.repositories.ConnectionManager
import com.marcohc.terminator.sample.data.repositories.VenueRepository
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.internal.operators.single.SingleJust
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

internal class GetVenueByIdUseCaseTest {

    @Mock
    lateinit var connectionManager: ConnectionManager

    @Mock
    private lateinit var venueRepository: VenueRepository

    private lateinit var useCase: GetVenueByIdUseCase

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        useCase = GetVenueByIdUseCase(
            connectionManager,
            venueRepository,
            Schedulers.trampoline()
        )
    }

    @Test
    fun `given connection when use case executes then return items`() {
        val city = "Madrid"
        val item = Venue()
        whenever(connectionManager.isConnected()).thenReturn(true)
        whenever(venueRepository.getByIdFromLocal(city)).thenReturn(SingleJust(item))
        whenever(venueRepository.getByIdFromNetwork(city)).thenReturn(SingleJust(item))
        whenever(venueRepository.save(item)).thenReturn(Completable.complete())

        useCase.execute(city).test().assertValue { it == item }
    }

    @Test
    fun `given connection when use case executes then save items`() {
        val city = "Madrid"
        val item = Venue()
        whenever(connectionManager.isConnected()).thenReturn(true)
        whenever(venueRepository.getByIdFromNetwork(city)).thenReturn(SingleJust(item))
        whenever(venueRepository.getByIdFromLocal(city)).thenReturn(SingleJust(item))

        useCase.execute(city).test()

        verify(venueRepository).save(item)
    }

    @Test
    fun `given no connection when use case executes then return local items`() {
        val city = "Madrid"
        val item = Venue()
        whenever(connectionManager.isConnected()).thenReturn(false)
        whenever(venueRepository.getByIdFromLocal(city)).thenReturn(SingleJust(item))

        useCase.execute(city).test().assertValue { it == item }
    }
}
