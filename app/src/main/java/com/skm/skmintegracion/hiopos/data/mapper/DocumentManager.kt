package com.skm.skmintegracion.hiopos.data.mapper

import com.skm.skmintegracion.hiopos.data.model.document.Document
import com.skm.skmintegracion.hiopos.data.model.document.payment_means.PaymentMean
import com.skm.skmintegracion.hiopos.data.model.document.header.Header
import com.skm.skmintegracion.hiopos.data.model.document.lines.Line
import org.simpleframework.xml.core.Persister
import java.io.StringReader
object DocumentManager {


    private val serializer = Persister()

    /**
     * Convierte un String con formato XML en un objeto Document.
     *
     * @param xmlString El contenido XML a parsear.
     * @return Un objeto Document si el parseo es exitoso, o null si ocurre un error.
     */
    fun fromXml(xmlString: String): Document? {
        return try {
            serializer.read(Document::class.java, StringReader(xmlString))
        } catch (e: Exception) {
            e.printStackTrace() // Idealmente, usa un logger
            null
        }
    }

    /**
     * Reemplaza la cabecera del documento.
     */
    fun updateHeader(original: Document, newHeader: Header): Document {
        return original.copy(header = newHeader)
    }

    /**
     * Añade una nueva línea a la lista de líneas del documento.
     */
    fun addLine(original: Document, newLine: Line): Document {
        // Crea una nueva lista añadiendo el nuevo elemento a la lista original.
        val updatedLines = original.lines?.plus(newLine)
        return original.copy(lines = updatedLines)
    }
    /**
     * !! NUEVO MÉTODO DE ALTO NIVEL !!
     * Actualiza un campo en `customDocPaymentMeanFields` para un PaymentMean específico
     * dentro de la lista de medios de pago del documento.
     *
     * Este método se encarga de toda la complejidad de la actualización anidada.
     *
     * @param original El documento base.
     * @param findPaymentMeanPredicate La condición para ENCONTRAR el PaymentMean a modificar.
     * @param key La clave del campo a añadir/actualizar en `customDocPaymentMeanFields`.
     * @param value El nuevo valor para esa clave.
     * @return Un nuevo Document con el campo anidado actualizado.
     */
    fun updateDocFieldInPaymentMean(
        original: Document,
        findPaymentMeanPredicate: (PaymentMean) -> Boolean,
        key: String,
        value: String?
    ): Document {
        // Usamos nuestro método 'updatePaymentMean' existente.
        return updatePaymentMean(
            original = original,
            // 1. Le pasamos el predicado para que encuentre el PaymentMean correcto.
            predicate = findPaymentMeanPredicate,
            // 2. Le decimos CÓMO transformar ese PaymentMean una vez que lo encuentre.
            transform = { paymentMeanToUpdate ->
                // Usamos el manager de PaymentMean para hacer la actualización final.
                // Esto nos devuelve una COPIA del PaymentMean con el campo del documento actualizado.
                PaymentMeanManager.addOrUpdateDocField(paymentMeanToUpdate, key, value)
            }
        )
    }

    /**
     * !! NUEVO !!
     * Actualiza un campo en `customFields` para un PaymentMean específico
     * dentro de la lista de medios de pago del documento.
     *
     * Este método se encarga de toda la complejidad de la actualización anidada.
     *
     * @param original El documento base.
     * @param findPaymentMeanPredicate La condición para ENCONTRAR el PaymentMean a modificar.
     * @param key La clave del campo a añadir/actualizar en `customFields`.
     * @param value El nuevo valor para esa clave.
     * @return Un nuevo Document con el campo anidado actualizado.
     */
    fun addOrUpdateCustomFieldInPaymentMean(
        original: Document,
        findPaymentMeanPredicate: (PaymentMean) -> Boolean,
        key: String,
        value: String?
    ): Document {
        return updatePaymentMean(
            original = original,
            // 1. Predicado para encontrar el PaymentMean a modificar.
            predicate = findPaymentMeanPredicate,
            // 2. Transformación a aplicar sobre el PaymentMean encontrado.
            transform = { paymentMeanToUpdate ->
                // Usamos el manager de PaymentMean para la actualización final.
                PaymentMeanManager.addOrUpdateCustomField(paymentMeanToUpdate, key, value)
            }
        )
    }

    /**
     * !! NUEVO !!
     * Elimina un medio de pago de la lista.
     * Usamos una función lambda (predicate) para que decidas cuál eliminar.
     *
     * @param original El documento base.
     * @param predicate Una condición que devuelve 'true' para el elemento a eliminar.
     * @return Un nuevo Document sin el PaymentMean.
     */
    fun removePaymentMean(original: Document, predicate: (PaymentMean) -> Boolean): Document {
        // filterNot crea una nueva lista excluyendo los elementos que cumplen la condición.
        val updatedList = original.paymentMeans?.filterNot(predicate)
        return original.copy(paymentMeans = updatedList)
    }

    /**
     * !! NUEVO Y AVANZADO !!
     * Modifica un medio de pago existente en la lista.
     *
     * @param original El documento base.
     * @param predicate La condición para ENCONTRAR el PaymentMean a modificar.
     * @param transform La función que se aplicará para ACTUALIZAR el PaymentMean encontrado.
     * @return Un nuevo Document con el PaymentMean modificado.
     */
    fun updatePaymentMean(
        original: Document,
        predicate: (PaymentMean) -> Boolean,
        transform: (PaymentMean) -> PaymentMean
    ): Document {
        val updatedList = original.paymentMeans?.map { paymentMean ->
            if (predicate(paymentMean)) {
                // Si este es el elemento que buscamos, aplicamos la transformación.
                transform(paymentMean)
            } else {
                // Si no, lo dejamos como está.
                paymentMean
            }
        }
        return original.copy(paymentMeans = updatedList)
    }
}