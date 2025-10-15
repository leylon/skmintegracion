package com.skm.skmintegracion.hiopos.data.model.document.payment_means

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "PaymentMean")
data class PaymentMean(
    @field:ElementMap(
        entry = "PaymentMeanField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var paymentMeanFields: Map<String, String?>? = null,

    @field:ElementMap(
        name = "CustomPaymentMeanFields",
        entry = "CustomPaymentMeanField",
        key = "Key",
        attribute = true,
        required = false,
    ) var customPaymentMeanFields: Map<String, String?>? = null,

    @field:ElementMap(
        name = "CustomDocPaymentMeanFields",
        entry = "CustomDocPaymentMeanField",
        key = "Key",
        attribute = true,
        required = false,
    ) var customDocPaymentMeanFields: Map<String, String?>? = null,

    @field:Element(
        name = "Currency",
        required = false,
    ) var currency: Currency? = null,

    @field:ElementMap(
        entry = "CurrencyField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var currencyFields: Map<String, String?>? = null,
)
