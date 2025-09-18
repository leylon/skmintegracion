package com.skm.skmintegracion.receipt.serialize


import com.skm.skmintegracion.receipt.Format
import com.skm.skmintegracion.receipt.Receipt
import com.skm.skmintegracion.receipt.ReceiptLine
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * Classe encarregada de serialitzar un objecte de tipus Receipt en format XML
 */
object ReceiptXMLSerializer {
    /**
     * Serialitza el comprovant seguint el format de l'API de cobraments electrònics
     * en format XML.
     *
     * @param receipt
     * @return
     */
    @Throws(ParserConfigurationException::class, TransformerException::class)
    fun serializeReceipt(receipt: Receipt): String {
        // Creem el node principal del comprobant
        val receiptDocument =
            DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
        val receiptNode = receiptDocument.createElement("Receipt")
        receiptNode.setAttribute("numCols", java.lang.String.valueOf(receipt.receiptColumns))

        receiptDocument.appendChild(receiptNode)


        // Afegim els nodes de les linies del comprobant
        for (receiptLine in receipt.getReceiptLines()) {
            // Creem i afegim el node de la linia del comprobant
            val receiptLineNode = receiptDocument.createElement("ReceiptLine")
            receiptNode.appendChild(receiptLineNode)


            // Afegim l'atribut que representa el tipus de linia
            receiptLineNode.setAttribute("type", receiptLine.lineType.name)


            // En el cas de que la linia sigui de tipus tall de paper no afegirem
            // ni el text ni els formats
            if (receiptLine.lineType === ReceiptLine.LineType.CUT_PAPER) continue


            // Creem i afegim el node que representa el text que conté la linia
            val textNode = receiptDocument.createElement("Text")
            receiptLineNode.appendChild(textNode)
            textNode.textContent = receiptLine.getLineText()


            // En el cas de que la linia sigui de tipus QR no afegirem els formats
            if (receiptLine.lineType === ReceiptLine.LineType.QR_CODE) continue


            // Afegim els nodes de format de la linia
            val formatsNode = receiptDocument.createElement("Formats")
            receiptLineNode.appendChild(formatsNode)


            // Si no hi ha formats afegim com a mínim el format "NORMAL"
            if (receiptLine.getFormats().isEmpty()) {
                val from = 0
                val to: Int = receipt.receiptColumns
                val formatType: Format.FormatType = Format.FormatType.NORMAL

                val formatNode = receiptDocument.createElement("Format")
                formatNode.setAttribute("from", from.toString())
                formatNode.setAttribute("to", to.toString())
                formatNode.textContent = formatType.name

                formatsNode.appendChild(formatNode)
            } else {
                for (format in receiptLine.getFormats()) {
                    val from: Int = format.from
                    val to: Int = format.to
                    val formatType: Format.FormatType = format.formatType!!

                    val formatNode = receiptDocument.createElement("Format")
                    formatNode.setAttribute("from", from.toString())
                    formatNode.setAttribute("to", to.toString())
                    formatNode.textContent = formatType.name

                    formatsNode.appendChild(formatNode)
                }
            }
        }


        // Retornem el comprobant serialitzat
        val transformer = TransformerFactory.newInstance().newTransformer()
        val result = StreamResult(StringWriter())
        val source = DOMSource(receiptDocument)
        transformer.transform(source, result)
        return result.writer.toString()
    }
}
