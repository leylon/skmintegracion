package com.skm.skmintegracion.hiopos.data.model.document.header

import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "Provider")
data class Provider(
    @field:ElementMap(
        entry = "ProviderField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var providerFields: Map<String, String?>? = null,

    @field:ElementMap(
        entry = "CustomProviderField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var customProviderFields: Map<String, String?>? = null,
)
