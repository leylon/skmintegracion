import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

// Clase Raíz: <ModifyDocumentResult>
@Root(name = "ModifyDocumentResult", strict = false)
data class ModifyDocumentResult(
    @field:Element(name = "PaymentMeans", required = false)
    var paymentMeans: PaymentMeans? = null
)

// Clase Contenedora: <PaymentMeans>
@Root(name = "PaymentMeans", strict = false)
data class PaymentMeans(
    @field:ElementList(name = "PaymentMean", inline = true, required = false)
    var paymentMeanList: List<PaymentMean> = emptyList()
)

// Clase de Objeto: <PaymentMean>
@Root(name = "PaymentMean", strict = false)
data class PaymentMean(
    @field:Element(name = "CustomDocPaymentMeanFields", required = false)
    var customPaymentMeanFields: CustomPaymentMeanFields? = null
)

// Clase Contenedora de Campos: <CustomPaymentMeanFields>
@Root(name = "CustomDocPaymentMeanFields", strict = false)
data class CustomPaymentMeanFields(
    @field:ElementList(name = "CustomDocPaymentMeanField", inline = true, required = false)
    var fields: List<CustomDocPaymentMeanField> = emptyList()
)

// Clase para cada campo: <CustomDocPaymentMeanField>
@Root(name = "CustomDocPaymentMeanField", strict = false)
data class CustomDocPaymentMeanField(
    @field:Attribute(name = "Key")
    var key: String = "",

    @field:Text
    var value: String = ""
)