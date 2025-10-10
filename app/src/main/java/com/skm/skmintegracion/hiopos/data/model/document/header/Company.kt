package com.skm.skmintegracion.hiopos.data.model.document.header

import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root

@Root(name = "Company")
data class Company(
    @field:ElementMap(
        entry = "CompanyField",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var companyFields: Map<String, String?>? = null,

    @field:ElementMap(
        name = "CustomCompanyFields",
        entry = "CustomCompanyField",
        key = "Key",
        attribute = true,
        required = false,
    ) var customCompanyFields: Map<String, String?>? = null,

    @field:ElementMap(
        name = "CustomAccountingCompanyFields",
        entry = "CustomAccountingCompanyField",
        key = "Key",
        attribute = true,
        required = false,
    ) var customAccountingCompanyFields: Map<String, String?>? = null,
)
