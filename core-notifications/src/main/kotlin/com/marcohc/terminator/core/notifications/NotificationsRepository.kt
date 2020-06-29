package com.marcohc.terminator.core.notifications

import com.google.firebase.messaging.RemoteMessage
import com.marcohc.terminator.core.utils.onNextCompletable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class NotificationsRepository {

    private val subject = PublishSubject.create<RemoteMessage>()

    fun observe(): Observable<RemoteMessage> = subject.hide()

    fun trigger(remoteMessage: RemoteMessage) = subject.onNextCompletable(remoteMessage)

}
