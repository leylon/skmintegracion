package com.skm.skmintegracion.hiopos.data.model.document.header

import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "Serie")
data class Serie(
    @field:ElementMap(
        entry = "SerieField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var serieFields: Map<String, String?>? = null,

    @field:ElementMap(
        name = "CustomSerieFields",
        entry = "CustomSerieField",
        key = "Key",
        attribute = true,
        required = false,
    ) var customSerieFields: Map<String, String?>? = null,
)
