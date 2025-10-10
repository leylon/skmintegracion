package com.skm.skmintegracion.hiopos.data.model.document.payment_means

import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "Currency")
data class Currency(
    @field:ElementMap(
        entry = "CurrencyField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var currencyFields: Map<String, String?>? = null,

    @field:ElementMap(
        name = "CustomCurrencyFields",
        entry = "CustomCurrencyField",
        key = "Key",
        attribute = true,
        required = false,
    ) var customCurrencyFields: Map<String, String?>? = null,
)
