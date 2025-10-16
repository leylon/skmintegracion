package com.skm.skmintegracion


import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.skm.skmintegracion.hiopos.data.CustomDocPaymentMeanField
import com.skm.skmintegracion.hiopos.data.FinalizeTransactionUseCase
import com.skm.skmintegracion.hiopos.data.HioposDataResponse
import com.skm.skmintegracion.hiopos.data.HioposSettingsManager
import com.skm.skmintegracion.hiopos.data.TransactionOutput
import com.skm.skmintegracion.hiopos.data.mapper.DocumentManager
import com.skm.skmintegracion.hiopos.data.mapper.PaymentMeanManager
import com.skm.skmintegracion.hiopos.data.model.document.Document
import com.skm.skmintegracion.hiopos.data.model.document.payment_means.PaymentMean
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class ProcessingViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val finalizeTransactionUseCase: FinalizeTransactionUseCase,
    private val settingsManager: HioposSettingsManager
) :  ViewModel() {

    private val _finalResultForAppA = MutableSharedFlow<List<CustomDocPaymentMeanField>>()
    val finalResultForAppA: SharedFlow<List<CustomDocPaymentMeanField>> = _finalResultForAppA.asSharedFlow()

    private val _inalResultResponse = MutableSharedFlow<TransactionOutput>()
    val inalResultResponse: SharedFlow<TransactionOutput> = _inalResultResponse.asSharedFlow()


    private val _finalResultHiopos = MutableSharedFlow<HioposDataResponse>()
    val finalResultHiopos : SharedFlow<HioposDataResponse> = _finalResultHiopos.asSharedFlow()

    private val _hiosDataResponse = MutableLiveData<HioposDataResponse>()
    val hiosDataResponse: MutableLiveData<HioposDataResponse> = _hiosDataResponse

    val savedHioposData: StateFlow<HioposDataResponse> = settingsManager.hioposDataFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HioposDataResponse("", "", "", "", "", "", "", "","","") // Valor inicial vacío
        )
    val savedDocumentData: StateFlow<String> = settingsManager.hioposDataDocument
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "" // Valor inicial vacío
        )


    // 2. Usar una clave para guardar y recuperar el dato
    companion object {
        private const val XML_DATA_KEY = "xml_data_key"
        const val IZIPAY_RESPONSE_CODE_KEY = "izipayResponseCode"
        const val IZIPAY_APPROVAL_CODE_KEY = "izipayApprovalCode"
        const val IZIPAY_CARD_NUMBER_KEY = "izipayCardNumber"
        const val IZIPAY_MESSAGE_KEY = "izipayMessage"
        const val IZIPAY_BRAND_KEY = "izipayBrand"
        const val IZIPAY_MERCHANT_ID_KEY = "izipayMerchantId"
        const val IZIPAY_ORDER_ID_KEY = "izipayOrderId"
        const val IZIPAY_TRANSACTION_ID_KEY = "izipayTransactionId"
        //const val IZYPAY_RESPONSE_STATUS = "izipayResponseStatus"
        const val IZIPAY_TERMINAL_ID_KEY = "izipayTerminalId"
        const val IZIPAY_NUM_INSTALLMENTS_KEY = "izipayNumInstallments"
    }

    init {
        // 3. Al iniciar, comprueba si hay datos guardados
        val savedXmlData: String? = savedStateHandle[XML_DATA_KEY]
        if (savedXmlData != null) {
            // Si el proceso murió y fue restaurado, retoma donde te quedaste
            //processInitialData(savedXmlData)
        }
    }

    fun saveAndProcessIzipayResponse(izipayExtras: Bundle) {
        // Extraer los datos que necesitas del Bundle
        val responseCode = izipayExtras.getString("responseCod")?: ""
        val approvalCode = izipayExtras.getString("approvalCode")?: ""
        val message = izipayExtras.getString("message")?: ""
        val cardNumber = izipayExtras.getString("card")?: ""
        val orderId = izipayExtras.getString("orderId")?: ""
        val brand = izipayExtras.getString("brand")?: ""
        val merchantId = izipayExtras.getString("merchantId")?: ""
        //val responseStatus = izipayExtras.getBoolean("isResponse")?: false
        val terminalId = izipayExtras.getString("terminalId")?: ""
        val numInstallments = izipayExtras.getString("numInstallments")?: "1"
        val transactionId = izipayExtras.getString("transactionId")?: ""

        // --- ¡AQUÍ ESTÁ LA MAGIA! ---
        // Guardas cada dato en el SavedStateHandle usando una clave.
        savedStateHandle[IZIPAY_RESPONSE_CODE_KEY] = responseCode
        savedStateHandle[IZIPAY_APPROVAL_CODE_KEY] = approvalCode
        savedStateHandle[IZIPAY_CARD_NUMBER_KEY] = cardNumber
        savedStateHandle[IZIPAY_MESSAGE_KEY] = message
        savedStateHandle[IZIPAY_BRAND_KEY] = brand
        savedStateHandle[IZIPAY_MERCHANT_ID_KEY] = merchantId
        savedStateHandle[IZIPAY_ORDER_ID_KEY] = orderId
        //savedStateHandle[IZYPAY_RESPONSE_STATUS] = responseStatus
        savedStateHandle[IZIPAY_TERMINAL_ID_KEY] = terminalId
        savedStateHandle[IZIPAY_NUM_INSTALLMENTS_KEY] = numInstallments
        savedStateHandle[IZIPAY_TRANSACTION_ID_KEY] = transactionId

        prepareOutputResponse(izipayExtras)
        prepareOutputForAppA()
        // Ahora que los datos están guardados de forma segura, puedes
        // continuar con la lógica para preparar la respuesta para la App A.
        // Por ejemplo, podrías emitir un nuevo estado a la Activity.
    }
    private fun prepareOutputForAppA() {
        viewModelScope.launch {
            // --- PASO 2.A: RECUPERAR ---
            // Lees los datos desde el SavedStateHandle usando las mismas claves.
            val responseCode = savedStateHandle.get<String>(IZIPAY_RESPONSE_CODE_KEY)
            val approvalCode = savedStateHandle.get<String>(IZIPAY_APPROVAL_CODE_KEY)
            val cardNumber = savedStateHandle.get<String>(IZIPAY_CARD_NUMBER_KEY)
            val message = savedStateHandle.get<String>(IZIPAY_MESSAGE_KEY)
            val brand = savedStateHandle.get<String>(IZIPAY_BRAND_KEY)
            val merchantId = savedStateHandle.get<String>(IZIPAY_MERCHANT_ID_KEY)
            val orderId = savedStateHandle.get<String>(IZIPAY_ORDER_ID_KEY)
            //val responseStatus = savedStateHandle.get<String>(IZYPAY_RESPONSE_STATUS)
            val terminalId = savedStateHandle.get<String>(IZIPAY_TERMINAL_ID_KEY)
            val numInstallments = savedStateHandle.get<String>(IZIPAY_NUM_INSTALLMENTS_KEY)
            val transactionId = savedStateHandle.get<String>(IZIPAY_TRANSACTION_ID_KEY)
            // --- PASO 2.B: PROCESAR (Usando el Caso de Uso) ---
            // Llama a tu Caso de Uso con los datos recuperados para que
            // aplique la lógica de negocio y cree el objeto final para la App A.
            val customFields = listOf(
                CustomDocPaymentMeanField(key = "SaleId", value = orderId.toString()),
                CustomDocPaymentMeanField(key = "LineNumber", value = "1"),
                CustomDocPaymentMeanField(key = "IdRef", value = transactionId.toString().takeLast(4)) ,
                CustomDocPaymentMeanField(key = "IdEntidad", value = "1"),
                CustomDocPaymentMeanField(key = "NTarjeta", value = cardNumber.toString()),
                CustomDocPaymentMeanField(key = "docTarjeta", value = terminalId.toString()),
                CustomDocPaymentMeanField(key = "IdFormaPagoKey", value = "3"),
                CustomDocPaymentMeanField(key = "IdTarjetaKey", value = brand.toString()),
                CustomDocPaymentMeanField(key = "Cuota", value = numInstallments.toString())
            )

            val result = customFields
            /*finalResultForAppA.emit(result)
            result.onSuccess { finalOutput ->
                // --- PASO 3: NOTIFICAR A LA ACTIVITY ---
                // Emite el objeto final para que la Activity lo recoja y lo envíe.
                _finalResultForAppA.emit(finalOutput)
            }*/
            val hioposDataResponse = HioposDataResponse(
                docTarjeta = terminalId.toString(),
                IdFormaPagoKey = "3",
                IdTarjetaKey = brand.toString(),
                IdRef =  transactionId.toString().takeLast(4),
                Ntarjeta = cardNumber.toString(),
                Cuota = numInstallments.toString(),
                IdEntidad = "1",
                SaleId = orderId.toString(),
                documentData = "",
                codStatus = responseCode.toString()
            )
            _hiosDataResponse.postValue(hioposDataResponse)
            println("HioposDataResponse: $hioposDataResponse")
            _finalResultHiopos.emit(hioposDataResponse)
        }
    }

    private fun prepareOutputResponse(izipayExtras: Bundle) {
        viewModelScope.launch {

            // ... (recuperas y procesas los datos como vimos antes)
            val result = finalizeTransactionUseCase(izipayExtras)
            println("finalizeTransactionUseCase: $result")
            result.onSuccess { finalOutput ->
                // 3. Cuando el resultado está listo, se EMITE al Flow.
                _inalResultResponse.emit(finalOutput)
            }
        }
    }

    fun saveSuccessfulTransactionData(response: HioposDataResponse) {
        // 3. Usa el viewModelScope para llamar a la función suspend del Manager.
        viewModelScope.launch {
            settingsManager.saveHioposData(response)
        }
    }
    fun saveDocumenData(documentData: String) {
        // 3. Usa el viewModelScope para llamar a la función suspend del Manager.
        viewModelScope.launch {
            settingsManager.saveDocumentData(documentData)
        }
    }

    fun clearSavedDocumentData() {
        // 3. Usa el viewModelScope para llamar a la función suspend del Manager.
        viewModelScope.launch {
            settingsManager.saveDocumentData("")
        }
    }

    fun clearSavedData() {
        // 3. Usa el viewModelScope para llamar a la función suspend del Manager.
        viewModelScope.launch {
            settingsManager.saveHioposData(HioposDataResponse("", "", "", "", "", "", "", "",""))
        }
    }

    fun updatePaymentMeanInfo(datoPaymentMean: String, hioposDataResponse :HioposDataResponse ): PaymentMean  {
        var documentData = DocumentManager.fromXml(datoPaymentMean)



        var paymentMean = PaymentMeanManager.fromXml(datoPaymentMean)!!

            paymentMean = PaymentMeanManager.addOrUpdatePaymentField(paymentMean, "IdRef", hioposDataResponse.IdRef)
            paymentMean = PaymentMeanManager.addOrUpdatePaymentField(paymentMean, "NTarjeta", hioposDataResponse.Ntarjeta)
            paymentMean = PaymentMeanManager.addOrUpdatePaymentField(paymentMean, "docTarjeta", hioposDataResponse.docTarjeta)
            paymentMean = PaymentMeanManager.addOrUpdatePaymentField(paymentMean, "IdFormaPagoKey", hioposDataResponse.IdFormaPagoKey)
            paymentMean = PaymentMeanManager.addOrUpdatePaymentField(paymentMean, "IdTarjetaKey", hioposDataResponse.IdTarjetaKey)
            paymentMean = PaymentMeanManager.addOrUpdatePaymentField(paymentMean, "Cuota", hioposDataResponse.Cuota)
            paymentMean = PaymentMeanManager.addOrUpdatePaymentField(paymentMean, "IdEntidad", hioposDataResponse.IdEntidad)
            paymentMean = PaymentMeanManager.addOrUpdatePaymentField(paymentMean, "SaleId", hioposDataResponse.SaleId)
        return paymentMean
    }

    fun updateDocumentInfo(datoPaymentMean: String, hioposDataResponse :HioposDataResponse ): Document  {
        var documentData = DocumentManager.fromXml(datoPaymentMean)

        documentData = DocumentManager.addOrUpdateCustomFieldInPaymentMean(
            original = documentData!!,
            findPaymentMeanPredicate = { pm ->
                pm.paymentMeanFields?.get("PaymenMeanName") == "IZIPAY"
            },
            key = "IdRef",
            value = hioposDataResponse.IdRef
        )
        documentData = DocumentManager.addOrUpdateCustomFieldInPaymentMean(
            original = documentData!!,
            findPaymentMeanPredicate = { pm ->
                pm.paymentMeanFields?.get("PaymenMeanName") == "IZIPAY"
            },
            key = "NTarjeta",
            value = hioposDataResponse.Ntarjeta
        )

        documentData = DocumentManager.addOrUpdateCustomFieldInPaymentMean(
            original = documentData!!,
            findPaymentMeanPredicate = { pm ->
                pm.paymentMeanFields?.get("PaymenMeanName") == "IZIPAY"
            },
            key = "docTarjeta",
            value = hioposDataResponse.docTarjeta
        )
        documentData = DocumentManager.addOrUpdateCustomFieldInPaymentMean(
            original = documentData!!,
            findPaymentMeanPredicate = { pm ->
                pm.paymentMeanFields?.get("PaymenMeanName") == "IZIPAY"
            },
            key = "IdFormaPagoKey",
            value = hioposDataResponse.IdFormaPagoKey
        )
        documentData = DocumentManager.addOrUpdateCustomFieldInPaymentMean(
            original = documentData!!,
            findPaymentMeanPredicate = { pm ->
                pm.paymentMeanFields?.get("PaymenMeanName") == "IZIPAY"
            },
            key = "IdTarjetaKey",
            value = hioposDataResponse.IdTarjetaKey
        )
        documentData = DocumentManager.addOrUpdateCustomFieldInPaymentMean(
            original = documentData!!,
            findPaymentMeanPredicate = { pm ->
                pm.paymentMeanFields?.get("PaymenMeanName") == "IZIPAY"
            },
            key = "Cuota",
            value = hioposDataResponse.Cuota
        )
        documentData = DocumentManager.addOrUpdateCustomFieldInPaymentMean(
            original = documentData!!,
            findPaymentMeanPredicate = { pm ->
                pm.paymentMeanFields?.get("PaymenMeanName") == "IZIPAY"
            },
            key = "IdEntidad",
            value = hioposDataResponse.IdEntidad
        )
        documentData = DocumentManager.addOrUpdateCustomFieldInPaymentMean(
            original = documentData!!,
            findPaymentMeanPredicate = { pm ->
                pm.paymentMeanFields?.get("PaymenMeanName") == "IZIPAY"
            },
            key = "SaleId",
            value = hioposDataResponse.SaleId
        )



        return documentData
    }
}