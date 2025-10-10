package com.skm.skmintegracion.hiopos.data.model.document.lines

import com.skm.skmintegracion.hiopos.data.model.document.lines.modifiers.Modifiers
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "Line")
data class Line(
    @field:ElementMap(
        entry = "LineField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var lineFields: Map<String, String?>? = null,

    @field:ElementList(
        name = "LineTaxes",
        entry = "LineTax",
        required = false,
    ) var lineTaxes: List<LineTax>? = null,

    @field:Element(
        name = "Modifiers",
        required = false,
    ) var modifiers: Modifiers? = null,

    @field:ElementMap(
        name = "CustomProductFields",
        entry = "CustomProductField",
        key = "Key",
        attribute = true,
        required = false,
    ) var customProductFields: Map<String, String?>? = null,

    @field:ElementMap(
        name = "CustomProductSizeFields",
        entry = "CustomProductSizeField",
        key = "Key",
        attribute = true,
        required = false,
    ) var customProductSizeFields: Map<String, String?>? = null,

    @field:ElementMap(
        name = "CustomDocLineFields",
        entry = "CustomDocLineField",
        key = "Key",
        attribute = true,
        required = false,
    ) var customDocLineFields: Map<String, String?>? = null,

    @field:ElementMap(
        name = "CustomDocLineSummaryFields",
        entry = "CustomDocLineSummaryField",
        key = "Key",
        attribute = true,
        required = false,
    ) var customDocLineSummaryFields: Map<String, String?>? = null,
)
