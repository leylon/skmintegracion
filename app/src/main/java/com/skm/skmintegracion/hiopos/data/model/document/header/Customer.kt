package com.skm.skmintegracion.hiopos.data.model.document.header

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "Customer")
data class Customer(
    @field:ElementMap(
        entry = "CustomerField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var customerFields: Map<String, String?>? = null,

    @field:ElementList(
        name = "Responsabilities",
        entry = "Responsability",
        required = false,
    ) var responsabilities: List<Responsability>? = null,

    @field:ElementMap(
        name = "CustomCustomerFields",
        entry = "CustomCustomerField",
        key = "Key",
        attribute = true,
        required = false,
    ) var customCustomerFields: Map<String, String?>? = null,
)
