package com.skm.skmintegracion.hiopos.data

import java.io.Serializable


data class HioposDataResponse(
    val docTarjeta: String,
    val IdFormaPagoKey: String,
    val IdTarjetaKey: String,
    val IdRef: String,
    val Ntarjeta: String,
    val Cuota: String,
    val IdEntidad: String,
    val SaleId: String
): Serializable
