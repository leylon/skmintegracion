package com.skm.skmintegracion.hiopos.data.model.document.lines.modifiers

import com.skm.skmintegracion.hiopos.data.model.document.lines.modifiers.LineTaxes
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "Modifiers")
data class Modifiers(
    @field:ElementList(
        entry = "Modifier",
        inline = true,
        required = false,
    ) var modifiers: List<Modifier>? = null,

    @field:ElementList(
        entry = "LineTaxes",
        inline = true,
        required = false,
    ) var lineTaxesList: List<LineTaxes>? = null,
)
