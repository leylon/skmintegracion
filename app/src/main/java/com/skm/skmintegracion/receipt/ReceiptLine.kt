package com.skm.skmintegracion.receipt

import java.util.LinkedList


/**
 * Representa una línia del comprobant.
 */
class ReceiptLine
    (
    /**
     * Tipus de linia
     */
    val lineType: LineType,
    /**
     * Text de la línia.
     */
    private val lineText: String?
) {
    /**
     * Tipus de linies suportades
     */
    enum class LineType {
        TEXT,
        CUT_PAPER,
        QR_CODE
    }

    /**
     * Formats que s'apliquen a la línia
     */
    private val formats: MutableList<Format> = LinkedList()


    /**
     * Afegim un format a la línia
     *
     * @param formaType format a aplicar
     * @param from des de quin caràcter s'aplica el format, comença per zero i és un format inclòs.
     * @param to fins a quin caràcter apliquem el format exclòs.
     */
    fun addFormat(formaType: Format.FormatType?, from: Int, to: Int) {
        formats.add(Format(formaType, from, to))
    }

    /**
     *
     * @return obtenim el llistat dels formats
     */
    fun getFormats(): List<Format> {
        return formats
    }

    /**
     *
     * @return El text de la línia.
     */
    fun getLineText(): String {
        return lineText ?: ""
    }
}
