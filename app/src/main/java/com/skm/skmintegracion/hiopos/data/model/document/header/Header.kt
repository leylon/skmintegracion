package com.skm.skmintegracion.hiopos.data.model.document.header

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "Header")
data class Header(
    @field:ElementMap(
        entry = "HeaderField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false
    ) var headerFields: Map<String, String?>? = null,

    @field:ElementMap(
        name = "CustomDocHeaderFields",
        entry = "CustomDocHeaderField",
        key = "Key",
        attribute = true,
        required = false
    ) var customDocHeaderFields: Map<String, String?>? = null,

    @field:Element(
        name = "Serie",
        required = false
    ) var serieObject: Serie? = null,

    @field:Element(
        name = "Discount",
        required = false
    ) var discount: Discount? = null,

    @field:Element(
        name = "ServiceCharge",
        required = false
    ) var serviceCharge: ServiceCharge? = null, // TODO: Complete this

    @field:Element(
        name = "Company",
        required = false,
    ) var company: Company? = null,

    @field:Element(
        name = "Shop",
        required = false,
    ) var shop: Shop? = null,

    @field:Element(
        name = "Seller",
        required = false,
    ) var seller: Seller? = null,

    @field:Element(
        name = "Customer",
        required = false,
    ) var customer: Customer? = null,

    @field:Element(
        name = "Provider",
        required = false,
    ) var provider: Provider? = null,

    @field:Element(
        name = "DocCurrency",
        required = false,
    ) var docCurrency: DocCurrency? = null,
)
