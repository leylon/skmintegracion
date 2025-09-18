package com.skm.skmintegracion.receipt


/**
 * Format d'una secció d'una línia
 */
class Format
    (
    /**
     * Format a aplicar
     */
    val formatType: FormatType?,
    /**
     * Inici d'on s'aplica el format, inclòs.
     */
    val from: Int,
    /**
     * Fi d'on s'aplica el format, exclòs.
     */
    val to: Int
) {
    /**
     * Diferents tipus de formats aplicables.
     */
    enum class FormatType {
        NORMAL,
        BOLD,
        DOUBLE_WIDTH,
        DOUBLE_HEIGHT,
        UNDERLINE
    }
}
