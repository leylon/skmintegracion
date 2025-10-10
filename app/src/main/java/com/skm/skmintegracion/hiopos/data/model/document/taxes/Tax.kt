package com.skm.skmintegracion.hiopos.data.model.document.taxes

import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "Tax")
data class Tax(
    @field:ElementMap(
        entry = "TaxField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var taxFields: Map<String, String?>? = null,

    @field:ElementMap(
        name = "CustomDocTaxFields",
        entry = "CustomDocTaxField",
        key = "Key",
        attribute = true,
        required = false,
    ) var customDocTaxFields: Map<String, String?>? = null,

    @field:ElementMap(
        name = "CustomTaxFields",
        entry = "CustomTaxField",
        key = "Key",
        attribute = true,
        required = false,
    ) var customTaxFields: Map<String, String?>? = null,
)
