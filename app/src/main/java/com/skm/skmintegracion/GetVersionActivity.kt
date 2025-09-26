package com.skm.skmintegracion

import android.app.Activity
import android.content.Intent
import android.os.Bundle


class GetVersionActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("ley: GetVersionActivity...onCreate")
        onGetVersion()
    }

    /**
     * Event triggered when a 'GET_VERSION' intent it's filtered. Is the responsibility
     * of integrator to implement it and set the right result.
     */
    protected fun onGetVersion() {
        setVersionResult(3)
    }


    protected fun setVersionResult(version: Int) {
        val resultIntent: Intent = Intent(getIntent().getAction())

        resultIntent.putExtra("Version", version)

        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}

