package com.skm.skmintegracion.hiopos.data.model.document

import com.skm.skmintegracion.hiopos.data.ModifyDocumentResult
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.ElementMap
import org.simpleframework.xml.Root
import com.skm.skmintegracion.hiopos.data.model.document.header.Header
import com.skm.skmintegracion.hiopos.data.model.document.lines.Line
import com.skm.skmintegracion.hiopos.data.model.document.taxes.Tax
import com.skm.skmintegracion.hiopos.data.model.document.payment_means.PaymentMean
import com.skm.skmintegracion.hiopos.data.model.document.used_coupons.UsedCoupon

@Root(name = "Document")
data class Document(
    @field:Element(
        name = "Header",
        required = false,
    ) var header: Header? = null,

    @field:ElementList(
        name = "Lines",
        entry = "Line",
        required = false,
    ) var lines: List<Line>? = null,

    @field:ElementList(
        name = "Taxes",
        entry = "Tax",
        required = false,
    ) var taxes: List<Tax>? = null,

    @field:ElementList(
        name = "PaymentMeans",
        entry = "PaymentMean",
        required = false,
    ) var paymentMeans: List<PaymentMean>? = null,

    @field:ElementMap(
        name = "AdditionalFields",
        entry = "AdditionalField",
        key = "Key",
        attribute = true,
        required = false,
    ) var additionalFields: Map<String, String?>? = null,

    @field:ElementList(
        name = "UsedCoupons",
        entry = "UsedCoupon",
        required = false,
    ) var usedCoupons: List<UsedCoupon>? = null,

    @field:ElementMap(
        entry = "Action",
        key = "Key",
        attribute = true,
        inline = true,
        required = false,
    ) var actions: Map<String, String?>? = null,
  /*  @field:Element(
        name = "ModifyDocumentResult", required = false)
    var modifyDocumentResult: ModifyDocumentResult? = null,
*/
    )
