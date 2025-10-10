package com.skm.skmintegracion.hiopos.data.model.document.header

import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "ServiceCharge")
data class ServiceCharge(
    @field:ElementMap(
        entry = "ServiceChargeField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var serviceChargeFields: Map<String, String?>? = null,
)
