package com.skm.skmintegracion

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import java.io.ByteArrayOutputStream


class GetCustomParamsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("ley: GetCustomParamsActivity...onCreate")
        val resultIntent = Intent(intent.action)
        try {
            // Obtenim els bytes del logo
            val baos = ByteArrayOutputStream()
            var readLen = 0
            val readBuffer = ByteArray(1024)
            val `is` = assets.open("logo.png")
            while ((`is`.read(readBuffer).also { readLen = it }) > 0) {
                baos.write(readBuffer, 0, readLen)
            }

            resultIntent.putExtra("Logo", baos.toByteArray())
            resultIntent.putExtra("Name", "izipay")

            setResult(RESULT_OK, resultIntent)
            finish()
        } catch (e: Exception) {
            resultIntent.putExtra("ErrorMessage", e.javaClass.toString() + " " + e.message)

            setResult(RESULT_CANCELED, resultIntent)
            finish()
        }
    }
}
