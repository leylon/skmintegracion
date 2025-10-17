package com.skm.skmintegracion

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.skm.skmintegracion.hiopos.data.FinalizeTransactionUseCase
import com.skm.skmintegracion.hiopos.data.HioposSettingsManager

class MainActivity : AppCompatActivity() {
    lateinit var btnBack: Button
    lateinit var btnReporteTotal: Button
    lateinit var btnReporteDetallado: Button
    lateinit var btnReportLastVoucher: Button
    private val IZIPAY_PACKAGE_NAME = "pe.izipay.pmpDev" // o "pe.izipay.izi" para producción [cite: 406, 407]
    private val IZIPAY_COMPONENT_NAME = "pe.izipay.Mpos00External" // Componente principal [cite: 408]
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        println("ley: MainActivity...onCreate")
        setContentView(R.layout.activity_blank)

        btnBack = findViewById(R.id.btnBack)
        btnReporteTotal = findViewById(R.id.btnReporteTotal)
        btnReporteDetallado = findViewById(R.id.btnReporteDetallado)
        btnReportLastVoucher = findViewById(R.id.btnReportLastVoucher)
        btnReporteTotal.setOnClickListener { solicitarReporteTotal() }
        btnReporteDetallado.setOnClickListener { solicitarReporteDetallado() }
        btnReportLastVoucher.setOnClickListener { obtenerUltimoVoucher() }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.blank)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        btnBack.setOnClickListener { finish() }
    }

    // --- FUNCIÓN PARA SOLICITAR REPORTE TOTAL ---
    private fun solicitarReporteTotal() {
        enviarPeticionDeReporte("50") // Usa el trxCode "50" para reporte total
    }

    // --- FUNCIÓN PARA SOLICITAR REPORTE DETALLADO ---
    private fun solicitarReporteDetallado() {
        enviarPeticionDeReporte("51") // Usa el trxCode "51" para reporte detallado
    }

    // --- 🧾 6. VOUCHERS ---
    private fun obtenerUltimoVoucher() {
        val extras = Bundle().apply {
            putString("trxCode", "56") // Código para Última transacción [cite: 218]
            //putBoolean("installments",true)
            putBoolean("flagEmail",true)
            putBoolean("flagPhone",true)
            //putString("orderId", saleId) // Tu ID de orden, será devuelto [cite: 105]
            //putBoolean("currency", true) // true: Soles, false: Dólares [cite: 105]
            putBoolean("voucherEnable", true) // Mostrar voucher/reporte en Izipay [cite: 108]

        }
        //println("ley: MainActivity...obtenerUltimoVoucher extras: $extras" )
        enviarPeticionAIzipay(extras)
    }

    private fun enviarPeticionAIzipay(extras: Bundle) {
        try {
            val intentHiopos = intent.extras
            val intent = Intent(Intent.ACTION_VIEW).apply {
                component = ComponentName(IZIPAY_PACKAGE_NAME, IZIPAY_COMPONENT_NAME)
                addCategory("android.intent.category.DEFAULT")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            // --- Parámetros base mandatorios para todas las peticiones ---
            extras.putString("packageName", this.javaClass.getPackage()?.name)
            extras.putString("packageId", applicationContext.packageName)
            extras.putString("activityResponse", this.javaClass.simpleName)
            println("ley: MainActivity...obtenerUltimoVoucher extras: $extras" )
            intent.putExtras(extras)
            //intentHiopos?.let { intent.putExtras(it) }
            startActivity(intent)
            //finish()

        } catch (e: Exception) {
            Toast.makeText(this, "Error: IziPay App no está instalada.", Toast.LENGTH_LONG).show()
        }
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