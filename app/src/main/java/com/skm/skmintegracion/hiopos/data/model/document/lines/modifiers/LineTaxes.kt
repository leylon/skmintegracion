package com.skm.skmintegracion.hiopos.data.model.document.lines.modifiers

import com.skm.skmintegracion.hiopos.data.model.document.lines.LineTax
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "LineTaxes")
data class LineTaxes(
    @field:ElementList(
        entry = "LineTax",
        inline = true,
        required = false,
    ) var lineTaxes: List<LineTax>? = null
)
