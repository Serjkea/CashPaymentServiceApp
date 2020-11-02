/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
package cashpaymentservice.datamodel

import scala.annotation.switch

case class PaymentStatus(var infoType: MessageType, var message: String) extends org.apache.avro.specific.SpecificRecordBase {
  def this() = this(null, "")
  def get(field$: Int): AnyRef = {
    (field$: @switch) match {
      case 0 => {
        infoType
      }.asInstanceOf[AnyRef]
      case 1 => {
        message
      }.asInstanceOf[AnyRef]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
  }
  def put(field$: Int, value: Any): Unit = {
    (field$: @switch) match {
      case 0 => this.infoType = {
        value
      }.asInstanceOf[MessageType]
      case 1 => this.message = {
        value.toString
      }.asInstanceOf[String]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
    ()
  }
  def getSchema: org.apache.avro.Schema = PaymentStatus.SCHEMA$
}

object PaymentStatus {
  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"PaymentStatus\",\"namespace\":\"cashpaymentservice.datamodel\",\"fields\":[{\"name\":\"infoType\",\"type\":{\"type\":\"enum\",\"name\":\"MessageType\",\"symbols\":[\"INFO\",\"WARN\"]}},{\"name\":\"message\",\"type\":\"string\"}]}")
}