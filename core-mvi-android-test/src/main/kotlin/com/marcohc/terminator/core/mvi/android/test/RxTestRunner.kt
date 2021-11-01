package com.marcohc.terminator.core.mvi.android.test

import androidx.test.runner.AndroidJUnitRunner
import com.squareup.rx2.idler.Rx2Idler
import io.reactivex.plugins.RxJavaPlugins

@Suppress("unused")
class RxTestRunner : AndroidJUnitRunner() {
    override fun onStart() {
        RxJavaPlugins.setInitIoSchedulerHandler(Rx2Idler.create("RxJava 2.x Io Scheduler"))
        super.onStart()
    }
}
