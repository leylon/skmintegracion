package com.skm.skmintegracion.hiopos.data.model.document.header

import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "DocCurrency")
data class DocCurrency(
    @field:ElementMap(
        entry = "DocCurrencyField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var docCurrencyFields: Map<String, String?>? = null,
)
