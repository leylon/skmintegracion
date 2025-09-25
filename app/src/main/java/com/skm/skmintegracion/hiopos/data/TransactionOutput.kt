package com.skm.skmintegracion.hiopos.data

data class TransactionOutput(
    val transactionResult: String, // ACCEPTED, FAILED, etc.
    val transactionType: String, // SALE, REFUND, etc.
    val authorizationId: String?,
    val cardNum : String? = "",
    val cardType : String? = "",
    val transactionData: String? = "",
    val amount: String? = "",
    val errorMessage: String? = "",
    val errorMessageTitle: String? = "",
    val hioposData: HioposDataResponse? = null
)
