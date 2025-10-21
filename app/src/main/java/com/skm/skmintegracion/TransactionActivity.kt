package com.skm.skmintegracion



import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.skm.skmintegracion.constants.Transaction
import com.skm.skmintegracion.constants.TransactionResult
import com.skm.skmintegracion.hiopos.data.CustomDocPaymentMeanField
import com.skm.skmintegracion.hiopos.data.CustomPaymentMeanFields
import com.skm.skmintegracion.hiopos.data.FinalizeTransactionUseCase
import com.skm.skmintegracion.hiopos.data.HioposDataResponse
import com.skm.skmintegracion.hiopos.data.HioposSettingsManager
import com.skm.skmintegracion.hiopos.data.ModifyDocumentResult
import com.skm.skmintegracion.hiopos.data.PaymentMean
import com.skm.skmintegracion.hiopos.data.PaymentMeans
import com.skm.skmintegracion.hiopos.data.mapper.toDocument
import com.skm.skmintegracion.hiopos.data.mapper.toXml
import com.skm.skmintegracion.utils.APIUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.StringWriter
import java.math.BigDecimal
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class TransactionActivity : AppCompatActivity() {
    // TextView per mostrar informació a la pantalla
    private val IZIPAY_PACKAGE_NAME = "pe.izipay.pmpDev" // o "pe.izipay.izi" para producción [cite: 406, 407]
    private val IZIPAY_COMPONENT_NAME = "pe.izipay.Mpos00External" // Componente principal [cite: 408]
    var saleId = ""
    // Necesario para el flujo de 2 pasos de la solicitud de BIN
    private var esperandoRespuestaDeBin = false
    private var datosOriginalesParaCompra: Bundle? = null

    private var textView: TextView? = null
    private var button2: Button? = null
    private var progressOverlay: LinearLayout? = null
    /*private val settingsManager: HioposSettingsManager by lazy {
        HioposSettingsManager(context = this)
    }
    private val finalizeTransactionUseCase  by lazy {
        FinalizeTransactionUseCase()
    }*/
    // 2. Crea la Factory, pasándole el UseCase.
    private val viewModelFactory: ProcessingViewModelFactory  by lazy {
        val app = application as MyApplication
        ProcessingViewModelFactory(app.finalizeTransactionUseCase,app.hioposSettingsManager)
    }
    private val viewModelProcess: ProcessingViewModel by viewModels {viewModelFactory}
    private var hioposDataResponseData: HioposDataResponse? = null
    private var hioposDocumentData: String? = ""
    private var customerEmail: String? = ""
    private var customerPhone: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("TransactionActivity onCreate called")
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)
        button2 = findViewById(R.id.button2)
        progressOverlay = findViewById(R.id.progress_overlay)

        button2?.setOnClickListener { onExit(it) }

        handleIntent(intent)

        lifecycleScope.launch {

            viewModelProcess.finalResultHiopos.collect { hioposDataResponse ->
                Log.d("HioposDataResponseActivity", hioposDataResponse.toString())
                hioposDataResponseData = hioposDataResponse
                viewModelProcess.hiosDataResponse.postValue(hioposDataResponseData)
                viewModelProcess.saveSuccessfulTransactionData(hioposDataResponse)
            }
        }

        lifecycleScope.launch {
            viewModelProcess.savedHioposData.collect { hioposData ->
                // Muestra el SaleId recuperado del mismo DataStore
                println("HioposDataResponseDataStore: $hioposData")
                //viewModelProcess.hiosDataResponse.postValue(hioposData)
                hioposDataResponseData = hioposData
                //viewModelProcess.hiosDataResponse.postValue(hioposDataResponseData)
            }
        }
        lifecycleScope.launch {
            viewModelProcess.savedDocumentData.collect { transactionOutput ->
                println("inalResultResponse: $transactionOutput")
                hioposDocumentData = transactionOutput
                if (transactionOutput.isNotEmpty()) { // Check if we have data to process
                    //showProgressAndExit()
                }
            }
        }
        //

    }


    private fun handleIntent(intent: Intent) {
        val transactionType = getIntent().getStringExtra("TransactionType")
        when (transactionType ?: "") {
            Transaction.SALE -> {
                // Obtenim els paràmetres d'entrada
                val document = getIntent().getStringExtra("DocumentData").toString().toDocument()

                val datass =getIntent().getStringExtra("DocumentData").toString()
                println("datas: $datass" )
                viewModelProcess.saveDocumenData(getIntent().getStringExtra("DocumentData").toString())
                saleId = document.header?.headerFields?.get("SaleId").toString()
                customerEmail = document.header?.customer?.customerFields?.get("Email") ?: ""
                customerPhone = document.header?.customer?.customerFields?.get("Phone") ?: ""
                println("SALE ID > $saleId")
                println("Customer Email > $customerEmail")
                println("Customer Phone > $customerPhone")

                val tenderType = getIntent().getStringExtra("TenderType")
                val currencyISO = getIntent().getStringExtra("CurrencyISO")
                val amount: BigDecimal? =
                    intent.getStringExtra("Amount")?.let { APIUtils.parseAPIAmount(it) }
                val tipAmount: BigDecimal? =
                    getIntent().getStringExtra("TipAmount")?.let { APIUtils.parseAPIAmount(it) }
                val taxAmount: BigDecimal? =
                    getIntent().getStringExtra("TaxAmount")?.let { APIUtils.parseAPIAmount(it) }
                val transactionId = getIntent().getIntExtra("TransactionId", -1)
                val transactionData = getIntent().getStringExtra("TransactionData")
                val receiptPrinterColumns = getIntent().getIntExtra("ReceiptPrinterColumns", 42)
                println(
                    """
                        PROCESSING SALE TRANSACTION > 
                        TenderType: $tenderType
                        CurrencyISO: $currencyISO
                        Amount: $amount
                        TipAmount: $tipAmount
                        TaxAmount: $taxAmount
                        TransactionId: $transactionId
                        TransactionData: $transactionData
                        ReceiptPrinterColumns: $receiptPrinterColumns
                        """.trimIndent()
                )
                /*textView!!.text = """
                PROCESSING SALE TRANSACTION > 
                TenderType: $tenderType
                CurrencyISO: $currencyISO
                Amount: $amount
                TipAmount: $tipAmount
                TaxAmount: $taxAmount
                TransactionId: $transactionId
                TransactionData: $transactionData
                ReceiptPrinterColumns: $receiptPrinterColumns
                """.trimIndent()
                */
                realizarCompra(amount.toString(), tipAmount.toString())
                //realizarCompraTest( amount.toString(), tipAmount.toString(),saleId,datass)
            }

            Transaction.NEGATIVE_SALE -> {
                val tenderType = getIntent().getStringExtra("TenderType")
                val amount: BigDecimal? =
                    getIntent().getStringExtra("Amount")?.let { APIUtils.parseAPIAmount(it) }
                val transactionId = getIntent().getIntExtra("TransactionId", -1)
                val receiptPrinterColumns = getIntent().getIntExtra("ReceiptPrinterColumns", -1)
            }

            Transaction.REFUND -> {
                val tenderType = getIntent().getStringExtra("TenderType")
                val amount: BigDecimal =
                    APIUtils.parseAPIAmount(getIntent().getStringExtra("Amount").toString())
                val transactionId = getIntent().getIntExtra("TransactionId", -1)
                val document = getIntent().getStringExtra("DocumentData").toString().toDocument()
                viewModelProcess.saveDocumenData(getIntent().getStringExtra("DocumentData").toString())
                saleId = document.header?.headerFields?.get("SaleId").toString()
                val transactionData = getIntent().getStringExtra("TransactionData")
                val receiptPrinterColumns = getIntent().getIntExtra("ReceiptPrinterColumns", 42)
                anularCompra(amount.toString(), "0001",saleId)
            }

            Transaction.ADJUST_TIPS -> {
                val tenderType = getIntent().getStringExtra("TenderType")
                val amount: BigDecimal =
                    APIUtils.parseAPIAmount(getIntent().getStringExtra("Amount").toString())
                val tipAmount: BigDecimal =
                    APIUtils.parseAPIAmount(getIntent().getStringExtra("TipAmount").toString())
                val transactionId = getIntent().getIntExtra("TransactionId", -1)
                val transactionData = getIntent().getStringExtra("TransactionData")
                val receiptPrinterColumns = getIntent().getIntExtra("ReceiptPrinterColumns", 42)
            }

            Transaction.VOID_TRANSACTION -> {
                val tenderType = getIntent().getStringExtra("TenderType")
                val amount: BigDecimal =
                    APIUtils.parseAPIAmount(getIntent().getStringExtra("Amount").toString())
                val taxAmount: BigDecimal =
                    APIUtils.parseAPIAmount(getIntent().getStringExtra("TaxAmount").toString())
                val tipAmount: BigDecimal =
                    APIUtils.parseAPIAmount(getIntent().getStringExtra("TipAmount").toString())
                val transactionId = getIntent().getIntExtra("TransactionId", -1)
                val receiptPrinterColumns = getIntent().getIntExtra("ReceiptPrinterColumns", 42)
            }

            Transaction.QUERY_TRANSACTION -> {
                val tenderType = getIntent().getStringExtra("TenderType")
                val amount: BigDecimal =
                    APIUtils.parseAPIAmount(getIntent().getStringExtra("Amount").toString())
                val taxAmount: BigDecimal =
                    APIUtils.parseAPIAmount(getIntent().getStringExtra("TaxAmount").toString())
                val tipAmount: BigDecimal =
                    APIUtils.parseAPIAmount(getIntent().getStringExtra("TipAmount").toString())
                val transactionId = getIntent().getIntExtra("TransactionId", -1)
                val receiptPrinterColumns = getIntent().getIntExtra("ReceiptPrinterColumns", 42)
            }

            Transaction.BATCH_CLOSE -> {

                //viewModelProcess.saveDocumenData(getIntent().getStringExtra("DocumentData").toString())

                realizarCierreDeCaja()
            }
            else -> {
                val izipayExtras = intent.extras
                if (izipayExtras != null && izipayExtras.getBoolean("isResponse")) {
                    println("izipayDataResponseIntentExtras: $izipayExtras")
                    // La Activity no guarda nada, solo delega la tarea al ViewModel
                    viewModelProcess.saveAndProcessIzipayResponse(izipayExtras)

                    println("hiopos: $hioposDataResponseData")

                } else {
                    // Manejar el caso de que no haya respuesta
                   // viewModelProcess.handleNoIzipayResponse()}
                    println("No hay respuesta de IZIPAY: ${izipayExtras?.getString("message").toString()}")
                    val resultIntent = Intent(intent.action)
                    resultIntent.putExtra("ErrorMessage", "Method not supported")
                    setResult(RESULT_CANCELED, resultIntent)
                    finish()
                }
                showProgressAndExit()
                //resultIntent.putExtra("ErrorMessage", "Method not supported")
                //setResult(RESULT_CANCELED, resultIntent)
                //finish()
            }
        }
    }

    fun processSale() {
        // Obtenim el tipus de transacció

        val transactionType = intent.getStringExtra("TransactionType")
        when (transactionType ?: "") {
            Transaction.SALE -> {
                // Obtenim els paràmetres d'entrada
                val tenderType = intent.getStringExtra("TenderType")
                val currencyISO = intent.getStringExtra("CurrencyISO")
                val amount: BigDecimal = APIUtils.parseAPIAmount(intent.getStringExtra("Amount").toString())
                val tipAmount: BigDecimal =
                    APIUtils.parseAPIAmount(intent.getStringExtra("TipAmount").toString())
                val taxAmount: BigDecimal =
                    APIUtils.parseAPIAmount(intent.getStringExtra("TaxAmount").toString())
                val transactionId = intent.getIntExtra("TransactionId", -1)
                val transactionData = intent.getStringExtra("TransactionData")
                val receiptPrinterColumns = intent.getIntExtra("ReceiptPrinterColumns", 42)
                val hioposDataResponse = intent.getSerializableExtra("hioposDataResponse") as? HioposDataResponse
                println("HioposDataResponseIntent: $hioposDataResponse")
                //hioposDataResponseData = hioposDataResponse
               // viewModelProcess.hiosDataResponse.postValue(hioposDataResponse)

                onSaleTransactionReceived(
                    tenderType, currencyISO, amount, tipAmount, taxAmount, transactionId,
                    transactionData, receiptPrinterColumns
                )
            }

            Transaction.NEGATIVE_SALE -> {
                val tenderType = intent.getStringExtra("TenderType")
                val amount: BigDecimal = APIUtils.parseAPIAmount(intent.getStringExtra("Amount").toString())
                val transactionId = intent.getIntExtra("TransactionId", -1)
                val receiptPrinterColumns = intent.getIntExtra("ReceiptPrinterColumns", -1)

                onNegativeSaleReceived(tenderType, amount, transactionId, receiptPrinterColumns)
            }

            Transaction.REFUND -> {
                val tenderType = intent.getStringExtra("TenderType")
                val amount: BigDecimal = APIUtils.parseAPIAmount(intent.getStringExtra("Amount").toString())
                val transactionId = intent.getIntExtra("TransactionId", -1)
                val transactionData = intent.getStringExtra("TransactionData")
                val receiptPrinterColumns = intent.getIntExtra("ReceiptPrinterColumns", 42)

                onRefundReceived(
                    tenderType,
                    amount,
                    transactionId,
                    transactionData,
                    receiptPrinterColumns
                )
            }

            Transaction.ADJUST_TIPS -> {
                val tenderType = intent.getStringExtra("TenderType")
                val amount: BigDecimal = APIUtils.parseAPIAmount(intent.getStringExtra("Amount").toString())
                val tipAmount: BigDecimal =
                    APIUtils.parseAPIAmount(intent.getStringExtra("TipAmount").toString())
                val transactionId = intent.getIntExtra("TransactionId", -1)
                val transactionData = intent.getStringExtra("TransactionData")
                val receiptPrinterColumns = intent.getIntExtra("ReceiptPrinterColumns", 42)

                onAdjustTipsReceived(
                    tenderType,
                    amount,
                    tipAmount,
                    transactionId,
                    transactionData,
                    receiptPrinterColumns
                )
            }

            Transaction.VOID_TRANSACTION -> {
                val tenderType = intent.getStringExtra("TenderType")
                val amount: BigDecimal = APIUtils.parseAPIAmount(intent.getStringExtra("Amount").toString())
                val taxAmount: BigDecimal =
                    APIUtils.parseAPIAmount(intent.getStringExtra("TaxAmount").toString())
                val tipAmount: BigDecimal =
                    APIUtils.parseAPIAmount(intent.getStringExtra("TipAmount").toString())
                val transactionId = intent.getIntExtra("TransactionId", -1)
                val receiptPrinterColumns = intent.getIntExtra("ReceiptPrinterColumns", 42)

                onVoidTransactionReceived(
                    tenderType,
                    amount,
                    taxAmount,
                    tipAmount,
                    transactionId,
                    receiptPrinterColumns
                )
            }

            Transaction.QUERY_TRANSACTION -> {
                val tenderType = intent.getStringExtra("TenderType")
                val amount: BigDecimal = APIUtils.parseAPIAmount(intent.getStringExtra("Amount").toString())
                val taxAmount: BigDecimal =
                    APIUtils.parseAPIAmount(intent.getStringExtra("TaxAmount").toString())
                val tipAmount: BigDecimal =
                    APIUtils.parseAPIAmount(intent.getStringExtra("TipAmount").toString())
                val transactionId = intent.getIntExtra("TransactionId", -1)
                val receiptPrinterColumns = intent.getIntExtra("ReceiptPrinterColumns", 42)

                onQueryTransactionReceived(
                    tenderType,
                    amount,
                    taxAmount,
                    tipAmount,
                    transactionId,
                    receiptPrinterColumns
                )
            }

            Transaction.BATCH_CLOSE -> onBatchCloseReceived()
            else -> onUnknownTransactionReceived()
        }
    }

    private fun showProgressAndExit() {
        progressOverlay?.visibility = View.VISIBLE
        lifecycleScope.launch {
            delay(3000) // Espera 5 segundos
            progressOverlay?.visibility = View.GONE
            onExit(null)
        }
    }

    fun onExit(view: View?) {
        processSale()
    }


    /* *********************************************************************** *
	 *                   TRANSACTION MANAGEMENT EVENTS                         *
	 * *********************************************************************** */
    /**
     * Event triggered when a sale transaction intent has thrown.
     */
    protected fun onSaleTransactionReceived(
        tenderType: String?, currencyISO: String?,
        amount: BigDecimal, tipAmount: BigDecimal, taxAmount: BigDecimal,
        transactionId: Int, transactionData: String?, receiptPrinterColumns: Int
    ) {
        val resultAmount = amount

        /*String customerReceipt = buildCustomerReceipt(transactionId);
		String merchantReceipt = buildMerchantReceipt(transactionId);

		setTransactionResult(
				TransactionResult.ACCEPTED, Transaction.SALE,
				resultAmount, tipAmount, taxAmount,
				"1", merchantReceipt, customerReceipt, "");

		 */
        setTransactionResult(
            TransactionResult.ACCEPTED, Transaction.SALE,
            resultAmount, tipAmount, taxAmount,
            "2", "", "", ""
        )
    }

    /**
     * Event triggered when a negative sale intent has thrown
     *
     * @param tenderType
     * @param amount
     * @param transactionId
     * @param receiptPrinterColumns
     */
    protected fun onNegativeSaleReceived(
        tenderType: String?, amount: BigDecimal?, transactionId: Int, receiptPrinterColumns: Int
    ) {
        setTransactionResult(
            TransactionResult.ACCEPTED, Transaction.NEGATIVE_SALE,
            amount, BigDecimal.ZERO, BigDecimal.ZERO,
            "", "", "", ""
        )
    }


    /**
     * Event triggered when a refund intent has thrown
     *
     * @param tenderType
     * @param amount
     * @param transactionId
     * @param transactionData
     * @param receiptPrinterColumns
     */
    protected fun onRefundReceived(
        tenderType: String?, amount: BigDecimal?, transactionId: Int, transactionData: String?,
        receiptPrinterColumns: Int
    ) {
        val merchantReceipt = buildMerchantReceipt(transactionId)

        setTransactionResult(
            TransactionResult.ACCEPTED, Transaction.REFUND,
            amount, BigDecimal.ZERO, BigDecimal.ZERO,
            "", merchantReceipt, "", ""
        )
    }


    /**
     * Event triggered when adjust tips intent has thrown
     *
     * @param tenderType
     * @param amount
     * @param tipAmount
     * @param transactionId
     * @param transactionData
     * @param receiptPrinterColumns
     */
    protected fun onAdjustTipsReceived(
        tenderType: String?,
        amount: BigDecimal?, tipAmount: BigDecimal, transactionId: Int,
        transactionData: String?, receiptPrinterColumns: Int
    ) {
        var tipAmount = tipAmount
        tipAmount = BigDecimal.ONE

        setTransactionResult(
            TransactionResult.ACCEPTED, Transaction.ADJUST_TIPS,
            amount, tipAmount, BigDecimal.ZERO,
            "", buildMerchantReceiptForAdjustTips(), "", ""
        )
    }


    /**
     * Event triggered when transaction void intent has thrown
     *
     * @param tenderType
     * @param amount
     * @param taxAmount
     * @param tipAmount
     * @param transactionId
     * @param receiptPrinterColumns
     */
    protected fun onVoidTransactionReceived(
        tenderType: String?, amount: BigDecimal?, taxAmount: BigDecimal?, tipAmount: BigDecimal?,
        transactionId: Int, receiptPrinterColumns: Int
    ) {
        setTransactionResult(
            TransactionResult.UNKNOWN_RESULT, Transaction.VOID_TRANSACTION,
            amount, BigDecimal.ZERO, BigDecimal.ZERO,
            "", "", "", ""
        )
    }

    /**
     * Event triggered when transaction query intent has thrown
     *
     * @param tenderType
     * @param amount
     * @param taxAmount
     * @param tipAmount
     * @param transactionId
     * @param receiptPrinterColumns
     */
    protected fun onQueryTransactionReceived(
        tenderType: String?, amount: BigDecimal?, taxAmount: BigDecimal?, tipAmount: BigDecimal?,
        transactionId: Int, receiptPrinterColumns: Int
    ) {
        val merchantReceipt = buildMerchantReceipt(transactionId)

        setTransactionResult(
            TransactionResult.ACCEPTED, Transaction.QUERY_TRANSACTION,
            amount, BigDecimal.ZERO, BigDecimal.ZERO,
            "", merchantReceipt, merchantReceipt, ""
        )
    }


    /**
     * Event triggered when a batch close intent has thrown
     */
    protected fun onBatchCloseReceived() {
        setBatchCloseResult(
            TransactionResult.ACCEPTED, Transaction.BATCH_CLOSE,
            BigDecimal(1835.99), 1, ""
        )
    }

    /**
     * Event triggered when an unknown transaction intent has thrown
     */
    private fun onUnknownTransactionReceived() {
        val resultIntent = Intent(intent.action)
        resultIntent.putExtra("ErrorMessage", "Method not supported")
        println("onUnknownTransactionReceived: Method not supported")
        hioposDataResponseData = viewModelProcess.hiosDataResponse.value
        var dataLocal = HioposDataResponse(
             docTarjeta =  hioposDataResponseData?.docTarjeta.toString(),
         IdFormaPagoKey = hioposDataResponseData?.IdFormaPagoKey.toString() ,
         IdTarjetaKey =  hioposDataResponseData?.IdTarjetaKey.toString(),
         IdRef =  hioposDataResponseData?.IdRef.toString(),
         Ntarjeta = hioposDataResponseData?.Ntarjeta.toString() ,
         Cuota =  hioposDataResponseData?.Cuota.toString(),
         IdEntidad = hioposDataResponseData?.IdEntidad.toString() ,
         SaleId = hioposDataResponseData?.SaleId.toString(),
         documentData = hioposDataResponseData?.documentData.toString(),
          codStatus = hioposDataResponseData?.codStatus.toString()
        )
        viewModelProcess.saveSuccessfulTransactionData(dataLocal)
        viewModelProcess.saveDocumenData(hioposDocumentData.toString())
        //viewModelProcess.saveDocumenData(hioposDocumentData.toString())
        resultIntent.putExtra("hioposDataResponse", hioposDataResponseData)
        println("Result: ${resultIntent.extras}")
        if (hioposDataResponseData?.codStatus != "00"){
            setResult(RESULT_CANCELED, resultIntent)
            finish()
        }else {
            setResult(RESULT_OK, resultIntent)
            finish()
        }

    }


    /* *********************************************************************** *
	 *                         ENDING TRANSACTION METHODS                      *
	 * *********************************************************************** */
    /**
     * Set result of current transaction.
     *
     * @param transactionResult
     * @param transactionType
     * @param amount
     * @param tipAmount
     * @param taxAmount
     * @param transactionData
     * @param merchantReceipt
     * @param customerReceipt
     * @param errorMessage
     */
    protected fun setTransactionResult(
        transactionResult: String?, transactionType: String?,
        amount: BigDecimal?, tipAmount: BigDecimal, taxAmount: BigDecimal,
        transactionData: String?, merchantReceipt: String?, customerReceipt: String?,
        errorMessage: String?
    ) {

        val resultIntent = Intent(intent.action)

        resultIntent.putExtra("TransactionResult", transactionResult)
        resultIntent.putExtra("TransactionType", transactionType)
        resultIntent.putExtra("Amount", amount?.let { APIUtils.serializeAPIAmount(it) })

        if (tipAmount.compareTo(BigDecimal.ZERO) != 0) resultIntent.putExtra(
            "TipAmount",
            APIUtils.serializeAPIAmount(tipAmount)
        )

        if (taxAmount.compareTo(BigDecimal.ZERO) != 0) resultIntent.putExtra(
            "TaxAmount",
            APIUtils.serializeAPIAmount(taxAmount)
        )

        /*if (transactionData != null && !transactionData.isEmpty()) resultIntent.putExtra(
            "TransactionData",
            transactionData
        )*/

        if (merchantReceipt != null && !merchantReceipt.isEmpty()) resultIntent.putExtra(
            "MerchantReceipt",
            merchantReceipt
        )

        if (customerReceipt != null && !customerReceipt.isEmpty()) resultIntent.putExtra(
            "CustomerReceipt",
            customerReceipt
        )

        if (errorMessage != null && !errorMessage.isEmpty()) resultIntent.putExtra(
            "ErrorMessage",
            errorMessage
        )

        println("HiosDataResponseUltimo: ${viewModelProcess.hiosDataResponse.value}")
        //hioposDataResponseData = viewModelProcess.hiosDataResponse.value

        val customFields = listOf(
            CustomDocPaymentMeanField(key = "SaleId", value = saleId),
            CustomDocPaymentMeanField(key = "LineNumber", value = "1"),
            CustomDocPaymentMeanField(key = "IdRef", value = hioposDataResponseData?.IdRef.toString()),
            CustomDocPaymentMeanField(key = "IdEntidad", value = hioposDataResponseData?.IdEntidad.toString()),
            CustomDocPaymentMeanField(key = "NTarjeta", value = hioposDataResponseData?.Ntarjeta.toString()),
            CustomDocPaymentMeanField(key = "docTarjeta", value = hioposDataResponseData?.docTarjeta.toString()),
            CustomDocPaymentMeanField(key = "IdFormaPagoKey", value = "3"),
            CustomDocPaymentMeanField(key = "IdTarjetaKey", value = hioposDataResponseData?.IdTarjetaKey.toString()),
            CustomDocPaymentMeanField(key = "Cuota", value = hioposDataResponseData?.Cuota.toString())
        )
        // Crear el objeto que contiene esos campos
        val customPaymentMeanFields = CustomPaymentMeanFields(fields = customFields)

        // Crear el objeto PaymentMean
        val paymentMean = PaymentMean(customPaymentMeanFields = customPaymentMeanFields)

        // Crear la lista de PaymentMeans
        val paymentMeans = PaymentMeans(paymentMeanList = listOf(paymentMean))

        // Crear el objeto raíz
        val documentToGenerate = ModifyDocumentResult(paymentMeans = paymentMeans)
        println("hioposDocumentData antes de convertir: ${hioposDocumentData.toString()}")
        //var hioposDocumentDatos = hioposDocumentData.toString().toDocument()
        //hioposDocumentDatos.modifyDocumentResult = ModifyDocumentResult(paymentMeans = paymentMeans)
       // val paymentMeans = com.skm.skmintegracion.hiopos.data.model.document.payment_means.PaymentMean()
        //hioposDocumentDatos.paymentMeans?.add(paymentMean)

        // hioposDocumentDatos = viewModelProcess.updateDocumentInfo(hioposDocumentData.toString(),hioposDataResponseData!!)
        //val paymentMeanData = hioposDocumentDatos.paymentMeans?.firstOrNull() ?: PaymentMean()
        //hioposDocumentDatos.paymentMeans = listOf(viewModelProcess.updatePaymentMeanInfo(paymentMeanData,hioposDataResponseData!!))


        //println("HioposDocumentDatos: $hioposDocumentDatos")
        //resultIntent.putExtra("DocumentData",hioposDocumentDatos.toXml())
        resultIntent.putExtra("TransactionData", "")
        resultIntent.putExtra("ModifyDocumentResult", documentToGenerate.toXml())
        println("RESULT INTENT > " + resultIntent.extras.toString())

        viewModelProcess.clearSavedData()
        viewModelProcess.clearSavedDocumentData()
        if (hioposDataResponseData?.codStatus.toString() != "00"){
            setTransactionResultWithException("No se pudo realizar la transacción, código ${hioposDataResponseData?.codStatus.toString()}")
        }else{
            setResult(RESULT_OK, resultIntent)
            finish()

        }



    }

    /**
     * Set result of batch close.
     *
     * @param transactionResult
     * @param transactionType
     * @param amount
     * @param batchNumber
     * @param errorMessage
     */
    protected fun setBatchCloseResult(
        transactionResult: String?, transactionType: String?, amount: BigDecimal,
        batchNumber: Int, errorMessage: String?
    ) {
        val resultIntent = Intent(intent.action)

        resultIntent.putExtra("TransactionResult", transactionResult)
        resultIntent.putExtra("TransactionType", transactionType)

        if (amount.compareTo(BigDecimal.ZERO) != 0) resultIntent.putExtra("Amount", amount)

        resultIntent.putExtra("BatchNumber", batchNumber.toString())

        if (errorMessage != null && !errorMessage.isEmpty()) resultIntent.putExtra(
            "ErrorMessage",
            errorMessage
        )

        setResult(RESULT_OK, resultIntent)
        finish()
    }

    /**
     * Set result as an Exception executing current action.
     *
     * @param errorMessage
     */
    protected fun setTransactionResultWithException(errorMessage: String?) {
        val resultIntent = Intent(intent.action)
        resultIntent.putExtra("ErrorMessage", errorMessage)
        setResult(RESULT_CANCELED, resultIntent)
        finish()
    }


    private fun buildCustomerReceipt(transactionId: Int): String {
        var customerReceipt = ""
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document = builder.newDocument()
            val receiptNode = document.createElement("Receipt")
            receiptNode.setAttribute("numCols", "42")
            document.appendChild(receiptNode)

            addReceiptLineToXMLDocument(
                document, receiptNode,
                "TEXT", "NORMAL",
                "CÓPIA CLIENTE¦§²³©®µãÃõÕ                       "
            )

            addReceiptLineToXMLDocument(
                document, receiptNode,
                "TEXT", "NORMAL",
                " !\";#$%&'()*+,-./                             "
            )

            addReceiptLineToXMLDocument(
                document, receiptNode,
                "TEXT", "NORMAL",
                "0123456789:;=>                                 "
            )

            addReceiptLineToXMLDocument(
                document, receiptNode,
                "TEXT", "NORMAL",
                "TransactionId=$transactionId"
            )

            addReceiptLineToXMLDocument(
                document, receiptNode,
                "CUT_PAPER", "NORMAL",
                ""
            )


            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()

            val source = DOMSource(document)

            val stringWriter = StringWriter()
            val streamResult = StreamResult(stringWriter)

            // Escrivim el document en un String per enviar-lo a HioPosCloud
            transformer.transform(source, streamResult)
            customerReceipt = stringWriter.toString()
        } catch (te: TransformerException) {
            println("TRANSFORMER EXCEPTION > " + te.message)
            te.printStackTrace()
        } catch (pce: ParserConfigurationException) {
            println("PARSER CONFIGURATION EXCEPTION > " + pce.message)
            pce.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return customerReceipt
    }

    private fun buildMerchantReceipt(transactionId: Int): String {
        var merchantReceipt = ""
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document = builder.newDocument()
            val receiptNode = document.createElement("Receipt")
            receiptNode.setAttribute("numCols", "42")
            document.appendChild(receiptNode)

            addReceiptLineToXMLDocument(
                document, receiptNode,
                "TEXT", "NORMAL",
                "CÓPIA COMERCIO¦§²³©®µãÃõÕ                       "
            )

            addReceiptLineToXMLDocument(
                document, receiptNode,
                "TEXT", "NORMAL",
                " !\";#$%&'()*+,-./                             "
            )

            addReceiptLineToXMLDocument(
                document, receiptNode,
                "TEXT", "NORMAL",
                "0123456789:;=>                                 "
            )

            addReceiptLineToXMLDocument(
                document, receiptNode,
                "TEXT", "NORMAL",
                "TransactionId=$transactionId"
            )

            addReceiptLineToXMLDocument(
                document, receiptNode,
                "CUT_PAPER", "NORMAL",
                ""
            )


            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()

            val source = DOMSource(document)

            val stringWriter = StringWriter()
            val streamResult = StreamResult(stringWriter)


            // Escrivim el document en un String per enviar-lo a HioPosCloud
            transformer.transform(source, streamResult)
            merchantReceipt = stringWriter.toString()
        } catch (te: TransformerException) {
            println("TRANSFORMER EXCEPTION > " + te.message)
            te.printStackTrace()
        } catch (pce: ParserConfigurationException) {
            println("PARSER CONFIGURATION EXCEPTION > " + pce.message)
            pce.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return merchantReceipt
    }

    fun buildMerchantReceiptForAdjustTips(): String {
        var merchantReceipt = ""
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document = builder.newDocument()
            val receiptNode = document.createElement("Receipt")
            receiptNode.setAttribute("numCols", "42")
            document.appendChild(receiptNode)


            addReceiptLineToXMLDocument(
                document, receiptNode,
                "TEXT", "NORMAL",
                "ADJUST TIPS TRANSACTION                        "
            )

            val transformerFactory = TransformerFactory.newInstance()
            val transformer = transformerFactory.newTransformer()

            val source = DOMSource(document)

            val stringWriter = StringWriter()
            val streamResult = StreamResult(stringWriter)


            // Escrivim el document en un String per enviar-lo a HioPosCloud
            transformer.transform(source, streamResult)
            merchantReceipt = stringWriter.toString()
        } catch (te: TransformerException) {
            te.printStackTrace()
        } catch (pce: ParserConfigurationException) {
            pce.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return merchantReceipt
    }

    private fun addReceiptLineToXMLDocument(
        document: Document, rootElement: Element,
        type: String, format: String, value: String
    ) {
        // ReceiptLine
        val receiptLine = document.createElement("ReceiptLine")
        receiptLine.setAttribute("type", type)
        rootElement.appendChild(receiptLine)


        // Formats
        val formatsNode = document.createElement("Formats")
        receiptLine.appendChild(formatsNode)

        val formatNode = document.createElement("Format")
        formatNode.setAttribute("from", "0")
        formatNode.setAttribute("to", "42")
        formatNode.textContent = format
        formatsNode.appendChild(formatNode)


        // Text
        val textNode = document.createElement("Text")
        textNode.textContent = value
        receiptLine.appendChild(textNode)
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

            intent.putExtras(extras)
            //intentHiopos?.let { intent.putExtras(it) }
            startActivity(intent)
            //finish()

        } catch (e: Exception) {
            Toast.makeText(this, "Error: IziPay App no está instalada.", Toast.LENGTH_LONG).show()
        }
    }
    private fun mostrarDialogo(titulo: String, contenido: String) {
        println("ley: mostrarDialogo called with title: $titulo and content: $contenido")
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(contenido)
            .setPositiveButton("Aceptar", null)
            .show()
    }
    // --- 💳 1. REALIZAR COMPRA (CON PROPINA OPCIONAL) ---
    private fun realizarCompra(amount: String, tipAmount: String = "0") {
        val extras = Bundle().apply {
            putString("trxCode", "01") // Código para Compra [cite: 100]
            putString("amount", amount) // [cite: 105]

            if (tipAmount.toDouble() > 0) {
                putString("tipAmount", tipAmount) // [cite: 105]
            }
            // --- Otros parámetros opcionales comunes ---
            //putBoolean("installments", false) // No solicitar cuotas [cite: 105]
            putBoolean("installments",true)
            putBoolean("flagEmail",true)
            putBoolean("flagPhone",true)
            putString("orderId", saleId) // Tu ID de orden, será devuelto [cite: 105]
            putBoolean("currency", true) // true: Soles, false: Dólares [cite: 105]
            putBoolean("voucherEnable", true) // Mostrar voucher/reporte en Izipay [cite: 108]

            if (!customerEmail.isNullOrEmpty()){
                putString("email",customerEmail)
            }
            if(!customerPhone.isNullOrEmpty()){
                putString("phone", customerPhone?.takeLast(9))
            }
        }

        enviarPeticionAIzipay(extras)
    }
    private fun realizarCompraTest(amount: String, tipAmount: String = "0", saleId: String, datass: String) {
        val extras = Bundle().apply {
            putString("trxCode", "01") // Código para Compra [cite: 100]
            putString("responseCod", "00") // [cite: 105]
            putString("approvalCode", amount) // [cite: 105]
            putString("card", "421355**3740") // [cite: 105]
            putString("orderId", saleId) // [cite: 105]
            putString("merchantId", "1") // [cite: 105]
            putString("terminalId", "TJ3324C521567") // [cite: 105]
            putString("numInstallments", "1") // [cite: 105]
            putString("brand", "MAST") // [cite: 105]


            if (tipAmount.toDouble() > 0) {
                putString("tipAmount", tipAmount) // [cite: 105]
            }
            // --- Otros parámetros opcionales comunes ---
            //putBoolean("installments", false) // No solicitar cuotas [cite: 105]
        }


        viewModelProcess.saveAndProcessIzipayResponse(extras)
        hioposDataResponseData = viewModelProcess.hiosDataResponse.value
        var dataLocal = HioposDataResponse(
            docTarjeta = "TJ3324C521567" ,
            IdFormaPagoKey = "3" ,
            IdTarjetaKey =  "MAST",
            IdRef =  saleId,
            Ntarjeta = "421355**3740" ,
            Cuota =  "1",
            IdEntidad = "1" ,
            SaleId = saleId,
            documentData = hioposDataResponseData?.documentData.toString(),
            codStatus = "00"
        )
        viewModelProcess.saveSuccessfulTransactionData(dataLocal)
        viewModelProcess.saveDocumenData(datass)
        //viewModelProcess.saveDocumenData(hioposDocumentData.toString())


    }

    // --- 🔄 2. ANULAR COMPRA ---
    private fun anularCompra(amount: String, referenceId: String,saleId: String) {
        val extras = Bundle().apply {
            putString("trxCode", "04") // Código para Anulación [cite: 186]
            putString("amount", amount) // Monto original [cite: 186]
            putString("reference", referenceId) // Referencia de la venta original a anular [cite: 186]
            putString("orderId", saleId)
            putBoolean("currency", true)
        }
        enviarPeticionAIzipay(extras)
    }

    // --- 🔍 3. SOLICITUD DE BIN (FLUJO DE 2 PASOS) ---
    // Paso 3.1: Iniciar la solicitud
    private fun solicitarBin(amount: String) {
        esperandoRespuestaDeBin = true
        datosOriginalesParaCompra = Bundle().apply {
            putString("amount", amount)
            putString("orderId", saleId)
        }
        val extras = Bundle().apply {
            putString("trxCode", "01") // Se inicia como una compra [cite: 100]
            putBoolean("flagGetBin", true) // Flag clave para solicitar BIN [cite: 108, 142]
            putString("amount", amount)
            putString("orderId", datosOriginalesParaCompra?.getString("orderId"))
        }
        enviarPeticionAIzipay(extras)
    }

    // Paso 3.2: Completar la compra después de recibir el BIN
    private fun completarCompraPostBin(finalAmount: String) {
        val extras = Bundle().apply {
            putString("trxCode", "01") // Se completa como una compra [cite: 100]
            putString("amount", finalAmount) // Monto final, posiblemente modificado
            putString("orderId", datosOriginalesParaCompra?.getString("orderId"))
        }
        enviarPeticionAIzipay(extras)
    }

    // --- ❌ 4. CANCELACIÓN DE SOLICITUD DE BIN ---
    private fun cancelarSolicitudBin() {
        val extras = Bundle().apply {
            putString("trxCode", "24") // Código para Cancelación de BIN [cite: 170]
            putString("orderId", datosOriginalesParaCompra?.getString("orderId"))
        }
        enviarPeticionAIzipay(extras)
    }

    // --- 📄 5. REPORTES ---
    private fun obtenerReporteTotal() {
        val extras = Bundle().apply {
            putString("trxCode", "50") // Código para Reporte Total [cite: 355]
        }
        enviarPeticionAIzipay(extras)
    }

    private fun obtenerReporteDetallado() {
        val extras = Bundle().apply {
            putString("trxCode", "51") // Código para Reporte Detallado [cite: 355]
        }
        enviarPeticionAIzipay(extras)
    }

    // --- 🧾 6. VOUCHERS ---
    private fun obtenerUltimoVoucher() {
        val extras = Bundle().apply {
            putString("trxCode", "56") // Código para Última transacción [cite: 218]
        }
        enviarPeticionAIzipay(extras)
    }

    private fun obtenerVoucherDuplicado(referenceId: String) {
        val extras = Bundle().apply {
            putString("trxCode", "57") // Código para Duplicado de voucher [cite: 259]
            putString("reference", referenceId) // Se necesita la referencia de la transacción
        }
        enviarPeticionAIzipay(extras)
    }

    // --- 📦 7. CIERRE DE CAJA ---
    private fun realizarCierreDeCaja() {
        val extras = Bundle().apply {
            // 1000131
            putString("trxCode", "08") // Código para Cierre de Caja [cite: 340]
            //putString("orderId", saleId)
        }
        enviarPeticionAIzipay(extras)
    }

    // --- 🏪 8. CONSULTA DE COMERCIOS ---
    private fun consultarComercios() {
        val extras = Bundle().apply {
            putString("trxCode", "99") // Código para Consulta de comercios [cite: 379]
        }
        enviarPeticionAIzipay(extras)
    }

/*
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // (como en onResume) use el intent más reciente.
        setIntent(intent)
        println("ley: onNewIntent called")

        handleIzipayResponse(intent)

    }
    private fun handleIzipayResponse(intent: Intent?) {
        val izipayExtras = intent?.extras
        if (izipayExtras != null && izipayExtras.getBoolean("isResponse")) {
            // Pasamos el Bundle al ViewModel para que lo procese
            //viewModel.onIzipayResponseReceived(izipayExtras)
            println("ley: Izipay response received: $izipayExtras" )
        } else {
            // Manejar el caso donde no hubo una respuesta válida de Izipay
            //viewModel.onIzipayResponseReceived(Bundle.EMPTY)
            println("ley: No valid Izipay response received." )
        }
    }
    */

    override fun onResume() {
        super.onResume()
        println("ley: onResume called" )
    }

    override fun onRestart() {
        super.onRestart()
        println("ley: onRestart called" )
        showProgressAndExit()
    }
}
