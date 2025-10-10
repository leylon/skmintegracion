package com.skm.skmintegracion.hiopos.data.model.document.lines

import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "LineTax")
data class LineTax(
    @field:ElementMap(
        entry = "LineTaxField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var lineTaxFields: Map<String, String?>? = null,

    @field:ElementMap(
        name = "CustomDocLineTaxFields",
        entry = "CustomDocLineTaxField",
        key = "Key",
        attribute = true,
        required = false,
    ) var customDocLineTaxFields: Map<String, String?>? = null,

    @field:ElementMap(
        name = "CustomTaxFields",
        entry = "CustomTaxField",
        key = "Key",
        attribute = true,
        required = false,
    ) var customTaxFields: Map<String, String?>? = null,
)
