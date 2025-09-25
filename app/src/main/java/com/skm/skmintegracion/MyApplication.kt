package com.skm.skmintegracion

import android.app.Application
import com.skm.skmintegracion.hiopos.data.FinalizeTransactionUseCase
import com.skm.skmintegracion.hiopos.data.HioposSettingsManager

class MyApplication : Application() {
    // Dependencias que vivirán durante toda la app
    val hioposSettingsManager by lazy { HioposSettingsManager(this) }
    val finalizeTransactionUseCase by lazy { FinalizeTransactionUseCase() }
}