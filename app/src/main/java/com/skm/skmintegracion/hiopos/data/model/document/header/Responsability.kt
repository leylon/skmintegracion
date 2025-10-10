package com.skm.skmintegracion.hiopos.data.model.document.header

import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "Responsability")
data class Responsability(
    @field:ElementMap(
        entry = "ResponsabilityField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var responsabilityFields: Map<String, String?>? = null,
)
