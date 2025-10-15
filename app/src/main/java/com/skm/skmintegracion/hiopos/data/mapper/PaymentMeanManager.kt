package com.skm.skmintegracion.hiopos.data.mapper

import com.skm.skmintegracion.hiopos.data.model.document.payment_means.Currency
import com.skm.skmintegracion.hiopos.data.model.document.payment_means.PaymentMean
import org.simpleframework.xml.core.Persister
import java.io.Reader
import java.io.StringReader

object PaymentMeanManager {
    private val serializer = Persister()

    /**
     * Convierte un String con formato XML en un objeto PaymentMean.
     *
     * @param xmlString El contenido XML a parsear.
     * @return Un objeto PaymentMean si el parseo es exitoso, o null si ocurre un error.
     */
    fun fromXml(xmlString: String): PaymentMean? {
        return try {
            val reader: Reader = StringReader(xmlString)
            serializer.read(PaymentMean::class.java, reader)
        } catch (e: Exception) {
            // En una app real, aquí deberías registrar el error (e.g., Log.e(...))
            e.printStackTrace()
            null
        }
    }

    /**
     * Crea una copia de un PaymentMean permitiendo actualizar cualquiera de sus propiedades.
     * Gracias a los parámetros por defecto, solo necesitas especificar lo que quieres cambiar.
     *
     * @param original El objeto PaymentMean base.
     * @param paymentMeanFields El nuevo mapa para paymentMeanFields.
     * @param customPaymentMeanFields El nuevo mapa para customPaymentMeanFields.
     * ... y así para todos los campos.
     * @return Una nueva instancia de PaymentMean con los datos actualizados.
     */
    fun update(
        original: PaymentMean,
        paymentMeanFields: Map<String, String?>? = original.paymentMeanFields,
        customPaymentMeanFields: Map<String, String?>? = original.customPaymentMeanFields,
        customDocPaymentMeanFields: Map<String, String?>? = original.customDocPaymentMeanFields,
        currency: Currency? = original.currency,
        currencyFields: Map<String, String?>? = original.currencyFields
    ): PaymentMean {
        return original.copy(
            paymentMeanFields = paymentMeanFields,
            customPaymentMeanFields = customPaymentMeanFields,
            customDocPaymentMeanFields = customDocPaymentMeanFields,
            currency = currency,
            currencyFields = currencyFields
        )
    }

    /**
     * Método de ayuda para añadir o actualizar un solo campo en `paymentMeanFields`.
     * Es más conveniente que reconstruir todo el mapa manualmente.
     *
     * @param original El objeto PaymentMean base.
     * @param key La clave del campo a añadir/actualizar.
     * @param value El valor del campo.
     * @return Una nueva instancia de PaymentMean con el campo actualizado.
     */
    fun addOrUpdatePaymentField(original: PaymentMean, key: String, value: String?): PaymentMean {
        // 1. Copia el mapa original a un mapa mutable.
        val updatedFields = original.paymentMeanFields?.toMutableMap()
        // 2. Añade o modifica el valor.
        updatedFields?.set(key, value)
        // 3. Usa el método copy() de la data class para crear un nuevo objeto con el mapa actualizado.
        return original.copy(paymentMeanFields = updatedFields)
    }

    /**
     * Método de ayuda para añadir o actualizar un solo campo en `customPaymentMeanFields`.
     */
    fun addOrUpdateCustomField(original: PaymentMean, key: String, value: String?): PaymentMean {
        val updatedFields = original.customDocPaymentMeanFields?.toMutableMap().apply {
            this?.set(key, value)
        }
        return original.copy(customDocPaymentMeanFields = updatedFields)
    }

    /**
     * Método de ayuda para añadir o actualizar un solo campo en `customDocPaymentMeanFields`.
     *
     * @param original El objeto PaymentMean base.
     * @param key La clave del campo a añadir/actualizar (e.g., "TerminalId", "TransactionId").
     * @param value El valor para esa clave.
     * @return Una nueva instancia de PaymentMean con el campo actualizado.
     */
    fun addOrUpdateDocField(original: PaymentMean, key: String, value: String?): PaymentMean {
        // 1. Copia el mapa original a uno mutable para poder modificarlo.
        val updatedFields = original.customDocPaymentMeanFields?.toMutableMap()

        // 2. Añade o actualiza la clave con su nuevo valor.
        updatedFields?.set(key, value)

        // 3. Llama a copy() para crear una nueva instancia inmutable con el mapa modificado.
        return original.copy(customDocPaymentMeanFields = updatedFields)
    }
}