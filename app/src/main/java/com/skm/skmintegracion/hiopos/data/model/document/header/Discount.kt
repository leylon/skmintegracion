package com.skm.skmintegracion.hiopos.data.model.document.header

import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "Discount")
data class Discount(
    @field:ElementMap(
        entry = "DiscountField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var discountFields: Map<String, String?>? = null,
)
