package com.marcohc.terminator.sample.data.repositories

import android.content.Context
import android.net.ConnectivityManager
import io.reactivex.Observable
import io.reactivex.Scheduler
import java.util.concurrent.TimeUnit

class ConnectionManager(
        private val context: Context,
        private val scheduler: Scheduler
) {

    fun isConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun observeConnection(): Observable<Boolean> {
        return Observable
            .interval(0, 1, TimeUnit.SECONDS, scheduler)
            .map { isConnected() }
            .distinctUntilChanged()
    }

}
