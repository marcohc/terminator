package com.marcohc.terminator.core.billing.domain

import com.marcohc.terminator.core.utils.onNextCompletable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class PurchaseEventBus {

    private val subject = PublishSubject.create<Unit>()

    fun triggerEvent() = subject.onNextCompletable(Unit)

    fun observe(): Observable<Unit> = subject.hide()
}
