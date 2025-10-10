package com.skm.skmintegracion.hiopos.data.model.document.used_coupons

import org.simpleframework.xml.ElementMap

data class UsedCoupon(
    @field:ElementMap(
        entry = "CouponField",
        required = false,
        inline = true,
        key = "Key",
        attribute = true,
    ) var couponFields: Map<String, String?>? = null,
)
