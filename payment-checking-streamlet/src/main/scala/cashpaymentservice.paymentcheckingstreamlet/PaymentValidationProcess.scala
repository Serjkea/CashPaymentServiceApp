package cashpaymentservice.paymentcheckingstreamlet

import cashpaymentservice.datamodel.{MessageType, PaymentData, PaymentStatus, ValidPayment}
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.flink.util.Collector

class PaymentValidationProcess(outputTag: OutputTag[PaymentStatus])
  extends ProcessFunction[PaymentData, ValidPayment] {
  override def processElement(
                               paymentData: PaymentData,
                               ctx: ProcessFunction[PaymentData, ValidPayment]#Context,
                               out: Collector[ValidPayment]
                             ): Unit = {
    if (isValid(paymentData)) {
      out.collect(buildValidPayment(paymentData))
    } else {
      ctx.output(outputTag, PaymentStatus(MessageType.WARN, s"Payment: ${paymentData.payment} - doesn't match the mask!"))
    }
  }

  def buildValidPayment(inputPayment: PaymentData): ValidPayment = {
    val mask = "\\w+".r
    val fields = mask.findAllIn(inputPayment.payment).toSeq
    ValidPayment("payment", fields.head, fields(1), fields(2).toInt)
  }

  def isValid(inputPayment: PaymentData): Boolean = {
    val mask = "<\\w+> -> <\\w+>: <\\d+>".r
    mask.findAllMatchIn(inputPayment.payment).nonEmpty
  }
}
