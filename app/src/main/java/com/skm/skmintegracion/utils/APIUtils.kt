package com.skm.skmintegracion.utils

import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.Properties
import javax.xml.parsers.DocumentBuilderFactory


object APIUtils {
    /**
     * Process int value that uses Payment Gateway
     *
     * @param value
     * @return
     */
    fun parseAPIAmount(value: String): BigDecimal {
        try {
            val integerPart = value.substring(0, value.length - 2)
            val decimalPart = value.substring(value.length - 2, value.length)

            return BigDecimal(("$integerPart.$decimalPart").toDouble())
        } catch (e: Exception) {
            return BigDecimal.ZERO
        }
    }

    /**
     * Formats decimal number to communication specified API format.
     *
     * @param bigDecimal
     * @return
     */
    fun serializeAPIAmount(bigDecimal: BigDecimal): String {
        val df = DecimalFormat("0.00", DecimalFormatSymbols(Locale.US))
        val result = df.format(bigDecimal.toDouble())

        return try {
            result.replace("\\.".toRegex(), "")
        } catch (nfe: NumberFormatException) {
            "000"
        }
    }

    /**
     * Parse initialize action input params to key - value structure.
     *
     * @param serializedPropeties
     * @return
     */
    fun parseInitializeParameters(serializedPropeties: String?): Properties {
        val properties = Properties()

        try {
            if (serializedPropeties != null && !serializedPropeties.isEmpty()) {
                val dbFactory = DocumentBuilderFactory.newInstance()
                val dBuilder = dbFactory.newDocumentBuilder()

                val document =
                    dBuilder.parse(ByteArrayInputStream(serializedPropeties.toByteArray()))
                document.normalize()


                // Obtaining all params
                val params = document.getElementsByTagName("Param")
                for (i in 0..<params.length) {
                    val param = params.item(i) as Element
                    val key = param.getAttribute("Key")
                    val value = param.textContent

                    properties[key] = value
                }
            }
        } catch (e: Exception) {
        }


        return properties
    }
}
