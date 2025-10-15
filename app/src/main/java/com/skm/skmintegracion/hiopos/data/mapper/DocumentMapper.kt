package com.skm.skmintegracion.hiopos.data.mapper



import com.skm.skmintegracion.hiopos.data.ModifyDocumentResult
import com.skm.skmintegracion.hiopos.data.PaymentMean
import com.skm.skmintegracion.hiopos.data.model.document.Document

import org.simpleframework.xml.core.Persister
import java.io.StringWriter

fun String.toDocument(): Document {
    val serializer = Persister()
    val document = serializer.read(Document::class.java, this)

    return document
}

fun Document.toXml(): String {
    val serializer = Persister()
    val writer = StringWriter()
    serializer.write(this, writer)

    return writer.toString()
}

fun PaymentMean.toXml(): String {
    val serializer = Persister()
    val writer = StringWriter()
    serializer.write(this, writer)

    return writer.toString()
}
fun ModifyDocumentResult.toXml(): String {
    val serializer = Persister()
    val writer = StringWriter()
    serializer.write(this, writer)
    return writer.toString()
}
