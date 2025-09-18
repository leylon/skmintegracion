package com.skm.skmintegracion

import android.app.Activity
import android.content.Intent
import android.os.Bundle


class BehaviorActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBehaviorResult()
    }

    protected fun setBehaviorResult() {
        val resultIntent = Intent(intent.action)

        resultIntent.putExtra("SupportsTransactionVoid", false)
        resultIntent.putExtra("SupportsTransactionQuery", true)
        resultIntent.putExtra("SupportsNegativeSales", true)
        resultIntent.putExtra("SupportsPartialRefund", false)
        resultIntent.putExtra("SupportsBatchClose", false)
        resultIntent.putExtra("SupportsTipAdjustment", false)
        resultIntent.putExtra("OnlyCreditForTipAdjustment", false)
        resultIntent.putExtra("SupportsCredit", false)
        resultIntent.putExtra("SupportsDebit", false)
        resultIntent.putExtra("SupportsEBTFoodstamp", false)
        resultIntent.putExtra("HasCustomParams", true)
        resultIntent.putExtra("GenerateNewTransactionIdByTransaction", false)

        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
