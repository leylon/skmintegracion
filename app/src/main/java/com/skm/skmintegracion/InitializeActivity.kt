package com.skm.skmintegracion


import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.skm.skmintegracion.utils.APIUtils
import java.util.Properties

class InitializeActivity : Activity() {
    lateinit var btnBack: Button
    lateinit var btnReporteTotal: Button
    lateinit var btnReporteDetallado: Button
    private val IZIPAY_PACKAGE_NAME = "pe.izipay.pmpDev" // o "pe.izipay.izi" para producción [cite: 406, 407]
    private val IZIPAY_COMPONENT_NAME = "pe.izipay.Mpos00External" // Componente principal [cite: 408]
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("ley: InitializeActivity...onCreate")
        //setContentView(R.layout.activity_blank)
        /*
        btnBack = findViewById(R.id.btnBack)
        btnReporteTotal = findViewById(R.id.btnReporteTotal)
        btnReporteDetallado = findViewById(R.id.btnReporteDetallado)
        btnReporteTotal.setOnClickListener { solicitarReporteTotal() }
        btnReporteDetallado.setOnClickListener { solicitarReporteDetallado() }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.blank)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
        /*btnBack.setOnClickListener { finishInitializeOK() } */
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

    // --- FUNCIÓN PARA SOLICITAR REPORTE TOTAL ---
    private fun solicitarReporteTotal() {
        enviarPeticionDeReporte("50") // Usa el trxCode "50" para reporte total
    }

    // --- FUNCIÓN PARA SOLICITAR REPORTE DETALLADO ---
    private fun solicitarReporteDetallado() {
        enviarPeticionDeReporte("51") // Usa el trxCode "51" para reporte detallado
    }

    private fun enviarPeticionDeReporte(trxCode: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                addCategory("android.intent.category.DEFAULT")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                component = ComponentName(IZIPAY_PACKAGE_NAME, IZIPAY_COMPONENT_NAME)
            }

            val extras = Bundle().apply {
                // --- Parámetros Mandatorios (M) ---
                putString("packageName", this.javaClass.getPackage()?.name)
                putString("packageId", applicationContext.packageName)
                putString("activityResponse", this.javaClass.getPackage()?.name)
                putString("trxCode", trxCode) // Código de transacción para el reporte

                // --- Parámetros Opcionales (O) ---
                putString("orderId", "REPORTE-${System.currentTimeMillis()}")
                putBoolean("voucherEnable", true) // Para que Izipay muestre el reporte/voucher
            }

            intent.putExtras(extras)
            startActivity(intent)

        } catch (e: Exception) {
            Toast.makeText(this, "Error: IziPay App no está instalada.", Toast.LENGTH_LONG).show()
        }
    }
}
