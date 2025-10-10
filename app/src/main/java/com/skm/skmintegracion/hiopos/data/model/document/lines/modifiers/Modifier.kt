package com.skm.skmintegracion.hiopos.data.model.document.lines.modifiers

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "Modifier")
data class Modifier(
    @field:ElementMap(
        entry = "ModifierField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var modifierFields: Map<String, String?>? = null,

    @field:ElementList(
        name = "Modifiers",
        entry = "Modifier",
        required = false,
    ) var modifiers: List<Modifier>? = null,

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
)
