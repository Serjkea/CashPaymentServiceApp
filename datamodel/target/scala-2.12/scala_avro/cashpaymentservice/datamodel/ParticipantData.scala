/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
package cashpaymentservice.datamodel

import scala.annotation.switch

case class ParticipantData(var opType: String, var nameId: String, var balance: Int) extends org.apache.avro.specific.SpecificRecordBase {
  def this() = this("", "", 0)
  def get(field$: Int): AnyRef = {
    (field$: @switch) match {
      case 0 => {
        opType
      }.asInstanceOf[AnyRef]
      case 1 => {
        nameId
      }.asInstanceOf[AnyRef]
      case 2 => {
        balance
      }.asInstanceOf[AnyRef]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
  }
  def put(field$: Int, value: Any): Unit = {
    (field$: @switch) match {
      case 0 => this.opType = {
        value.toString
      }.asInstanceOf[String]
      case 1 => this.nameId = {
        value.toString
      }.asInstanceOf[String]
      case 2 => this.balance = {
        value
      }.asInstanceOf[Int]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
    ()
  }
  def getSchema: org.apache.avro.Schema = ParticipantData.SCHEMA$
}

object ParticipantData {
  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ParticipantData\",\"namespace\":\"cashpaymentservice.datamodel\",\"fields\":[{\"name\":\"opType\",\"type\":\"string\"},{\"name\":\"nameId\",\"type\":\"string\"},{\"name\":\"balance\",\"type\":\"int\"}]}")
}