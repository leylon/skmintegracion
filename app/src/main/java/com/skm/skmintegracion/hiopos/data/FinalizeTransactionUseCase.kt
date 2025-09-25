package com.skm.skmintegracion.hiopos.data

import android.os.Bundle

class FinalizeTransactionUseCase { /**
 * El operador invoke permite que la clase sea llamada como si fuera una función.
 * @param izipayExtras El Bundle crudo recibido desde la app de Izipay.
 * @return Un objeto Result que contiene el TransactionOutput listo para ser enviado.
 */
operator fun invoke(izipayExtras: Bundle): Result<TransactionOutput> {
    return try {
        val responseCod = izipayExtras.getString("responseCod")
        val message = izipayExtras.getString("message")

        if (responseCod == "00") {
            // --- TRANSACCIÓN EXITOSA ---

            // 1. Construir el objeto con los datos específicos para el XML de Hiopos.
            val hioposData = createHioposDataResponse(izipayExtras)

            // 2. Construir el objeto de salida completo.
            val finalOutput = TransactionOutput(
                transactionResult = "ACCEPTED",
                transactionType = "SALE",
                authorizationId = izipayExtras.getString("approvalCode"),
                amount = izipayExtras.getString("amount")?.replace(".", ""), // Ajustar formato
                cardNum = izipayExtras.getString("card"),
                cardType = izipayExtras.getString("brand"),
                transactionData = izipayExtras.getString("transactionId"),
                hioposData = hioposData // Anidar el objeto con datos para el XML
            )
            Result.success(finalOutput)

        } else {
            // --- TRANSACCIÓN FALLIDA ---
            val finalOutput = TransactionOutput(
                transactionResult = "FAILED",
                transactionType = "SALE",
                errorMessage = message ?: "Transacción denegada por Izipay.",
                errorMessageTitle = "Error en la transacción",
                amount =  "", // Ajustar formato
                cardNum = "",
                cardType = "",
                transactionData = "",
                authorizationId = "",
                hioposData = null ,
            )
            Result.success(finalOutput)
        }
    } catch (e: Exception) {
        // Capturar cualquier error inesperado durante el procesamiento.
        Result.failure(e)
    }
}

    /**
     * Método privado para crear y poblar el objeto HioposDataResponse.
     * Este objeto contiene exclusivamente los datos necesarios para generar el XML ModifyDocumentResult.
     */
    private fun createHioposDataResponse(izipayExtras: Bundle): HioposDataResponse {
        // La respuesta de Izipay no contiene todos los campos que Hiopos necesita
        // para el XML (como IdRef, IdEntidad, etc.). Aquí deberás poner valores
        // por defecto o lógicos según tu caso de negocio.

        return HioposDataResponse(
            // --- Datos que SÍ vienen de Izipay ---
            docTarjeta = izipayExtras.getString("transactionId", ""), // O el campo más apropiado
            Ntarjeta = izipayExtras.getString("card", ""),
            IdTarjetaKey = izipayExtras.getString("brand", "UNKNOWN"),
            SaleId = izipayExtras.getString("orderId", ""),

            // --- Datos que NO vienen de Izipay y deben ser definidos por tu lógica ---
            IdFormaPagoKey = "3", // Valor fijo o basado en alguna lógica
            IdRef = "0000",       // Valor por defecto o de tu sistema
            Cuota = izipayExtras.getString("numInstallments", "1"),
            IdEntidad = "1"       // Valor por defecto o de tu sistema
        )
    }
}