package com.skm.skmintegracion


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.skm.skmintegracion.utils.APIUtils
import java.util.Properties

class InitializeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val properties: Properties =
            APIUtils.parseInitializeParameters(intent.getStringExtra("Parameters"))
        onInitialize(properties)
    }

    protected fun onInitialize(cloudLicenseProperties: Properties) {
        // List all received properties
        for (objKey in cloudLicenseProperties.keys) {
            val key = objKey as String

            println(
                "CLOUD LICENSE PROPERTY " + key + ": VALUE = " + cloudLicenseProperties.getProperty(
                    key
                )
            )
        }

        finishInitializeOK()
    }

    protected fun finishInitializeOK() {
        val resultItent = Intent(intent.action)
        setResult(RESULT_OK, resultItent)
        finish()
    }

    protected fun finishInitializeWithError(errorMessage: String?) {
        val resultIntent = Intent(intent.action)
        resultIntent.putExtra("ErrorMessage", errorMessage)

        setResult(RESULT_CANCELED, resultIntent)
        finish()
    }
}
