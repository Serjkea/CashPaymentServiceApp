package cashpaymentservice.paymentcheckingstreamlet

import cashpaymentservice.datamodel.{ PaymentData, PaymentStatus, ValidPayment }
import cloudflow.flink._
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.{ AvroInlet, AvroOutlet }
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.{ DataStream, OutputTag }

class PaymentCheckingStreamlet extends FlinkStreamlet {

  @transient val paymentsIn: AvroInlet[PaymentData] = AvroInlet("payments-in")

  @transient val checkStatusOut: AvroOutlet[PaymentStatus]  = AvroOutlet("check-status-out")
  @transient val validPaymentsOut: AvroOutlet[ValidPayment] = AvroOutlet("valid-payments-out")

  @transient val shape: StreamletShape = StreamletShape(paymentsIn).withOutlets(checkStatusOut, validPaymentsOut)

  override protected def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {
    override def buildExecutionGraph(): Unit = {

      val inputPayment: DataStream[PaymentData] = readStream(paymentsIn)

      val outputTag = new OutputTag[PaymentStatus]("warning-branch")

      val outputValidPayment = inputPayment.process(new PaymentValidationProcess(outputTag))

      val outputPaymentStatus = outputValidPayment.getSideOutput(outputTag)

      writeStream(validPaymentsOut, outputValidPayment)
      writeStream(checkStatusOut, outputPaymentStatus)

    }
  }
}
