package com.skm.skmintegracion.receipt

import java.util.LinkedList


/**
 * Entitat que gestiona la creació d'un comprobant de targeta
 */
class Receipt
    (val receiptColumns: Int) {
    private val receiptLines: MutableList<ReceiptLine> = LinkedList<ReceiptLine>()

    /**
     * Afegeix una línia de text.
     *
     * @param text
     * @return
     */
    fun addTextLine(text: String?): ReceiptLine {
        val receiptLine: ReceiptLine = ReceiptLine(ReceiptLine.LineType.TEXT, text)
        receiptLines.add(receiptLine)
        return receiptLine
    }

    /**
     * Afegeix un QR al comprobant.
     *
     * @param qrInfo
     */
    fun addQrLine(qrInfo: String?) {
        val receiptLine: ReceiptLine = ReceiptLine(ReceiptLine.LineType.QR_CODE, qrInfo)
        receiptLines.add(receiptLine)
    }

    /**
     * Afegeix un tall de línia.
     */
    fun addCutLine() {
        val receiptLine: ReceiptLine = ReceiptLine(ReceiptLine.LineType.CUT_PAPER, "")
        receiptLines.add(receiptLine)
    }

    fun getReceiptLines(): List<ReceiptLine> {
        return receiptLines
    }
}
