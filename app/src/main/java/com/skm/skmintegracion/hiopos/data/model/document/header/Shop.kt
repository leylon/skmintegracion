package com.skm.skmintegracion.hiopos.data.model.document.header

import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "Shop")
data class Shop(
    @field:ElementMap(
        entry = "ShopField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var shopFields: Map<String, String?>? = null,

    @field:ElementMap(
        name = "CustomShopFields",
        entry = "CustomShopField",
        key = "Key",
        attribute = true,
        required = false,
    ) var customShopFields: Map<String, String?>? = null,
)
