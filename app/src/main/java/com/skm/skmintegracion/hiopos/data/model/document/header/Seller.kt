package com.skm.skmintegracion.hiopos.data.model.document.header

import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "Seller")
data class Seller(
    @field:ElementMap(
        entry = "SellerField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var sellerFields: Map<String, String?>? = null,
)
