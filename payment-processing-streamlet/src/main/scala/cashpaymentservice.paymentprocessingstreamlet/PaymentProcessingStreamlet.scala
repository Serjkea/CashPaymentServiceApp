package cashpaymentservice.paymentprocessingstreamlet

import cashpaymentservice.datamodel.{ ParticipantData, PaymentStatus, ValidPayment }
import cloudflow.flink._
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.{ AvroInlet, AvroOutlet }
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.DataStream

class PaymentProcessingStreamlet extends FlinkStreamlet {

  @transient val participantsIn: AvroInlet[ParticipantData] = AvroInlet("participants-in")
  @transient val validPaymentsIn: AvroInlet[ValidPayment]   = AvroInlet("valid-payments-in")

  @transient val processStatusOut: AvroOutlet[PaymentStatus] = AvroOutlet("process-status-out")

  @transient val shape: StreamletShape = StreamletShape(processStatusOut).withInlets(participantsIn, validPaymentsIn)

  override protected def createLogic(): FlinkStreamletLogic = new FlinkStreamletLogic() {
    override def buildExecutionGraph(): Unit = {

      val inputParticipant: DataStream[ParticipantData] = readStream(participantsIn)

      val inputValidPayment: DataStream[ValidPayment] = readStream(validPaymentsIn)

      val outputPaymentStatus: DataStream[PaymentStatus] =
        inputParticipant
          .connect(inputValidPayment)
          .keyBy(pd => pd.opType, vp => vp.opType)
          .process(new MakingPayment)

      writeStream(processStatusOut, outputPaymentStatus)
    }
  }
}

