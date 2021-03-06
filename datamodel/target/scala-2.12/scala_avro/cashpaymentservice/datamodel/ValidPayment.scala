/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
package cashpaymentservice.datamodel

import scala.annotation.switch

case class ValidPayment(var opType: String, var from: String, var recipient: String, var value: Int) extends org.apache.avro.specific.SpecificRecordBase {
  def this() = this("", "", "", 0)
  def get(field$: Int): AnyRef = {
    (field$: @switch) match {
      case 0 => {
        opType
      }.asInstanceOf[AnyRef]
      case 1 => {
        from
      }.asInstanceOf[AnyRef]
      case 2 => {
        recipient
      }.asInstanceOf[AnyRef]
      case 3 => {
        value
      }.asInstanceOf[AnyRef]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
  }
  def put(field$: Int, value: Any): Unit = {
    (field$: @switch) match {
      case 0 => this.opType = {
        value.toString
      }.asInstanceOf[String]
      case 1 => this.from = {
        value.toString
      }.asInstanceOf[String]
      case 2 => this.recipient = {
        value.toString
      }.asInstanceOf[String]
      case 3 => this.value = {
        value
      }.asInstanceOf[Int]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
    ()
  }
  def getSchema: org.apache.avro.Schema = ValidPayment.SCHEMA$
}

object ValidPayment {
  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ValidPayment\",\"namespace\":\"cashpaymentservice.datamodel\",\"fields\":[{\"name\":\"opType\",\"type\":\"string\"},{\"name\":\"from\",\"type\":\"string\"},{\"name\":\"recipient\",\"type\":\"string\"},{\"name\":\"value\",\"type\":\"int\"}]}")
}