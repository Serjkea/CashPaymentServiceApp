package cashpaymentservice.paymentprocessingstreamlet

import cashpaymentservice.datamodel.{ ParticipantData, PaymentStatus, ValidPayment }
import cloudflow.flink._
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.{ AvroInlet, AvroOutlet }
import org.apache.flink.api.common.state.{ MapState, MapStateDescriptor }
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.util.Collector

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

class MakingPayment extends KeyedCoProcessFunction[String, ParticipantData, ValidPayment, PaymentStatus] {

  @transient lazy val stateDescriptor =
    new MapStateDescriptor[String, Int]("participantsBalance", classOf[String], classOf[Int])
  @transient lazy val participantsBalance: MapState[String, Int] = getRuntimeContext.getMapState(stateDescriptor)

  def processElement1(
    participant: ParticipantData,
    ctx: KeyedCoProcessFunction[String, ParticipantData, ValidPayment, PaymentStatus]#Context,
    out: Collector[PaymentStatus]
  ): Unit = {
    participantsBalance.put(participant.nameId, participant.balance)
    out.collect(PaymentStatus("INFO", s"For participant: ${participant.nameId} - balance updated!"))
  }

  def processElement2(
    payment: ValidPayment,
    ctx: KeyedCoProcessFunction[String, ParticipantData, ValidPayment, PaymentStatus]#Context,
    out: Collector[PaymentStatus]
  ): Unit = {
    out.collect(makePayment(payment, participantsBalance))
  }

  def makePayment(payment: ValidPayment, participantsBalance: MapState[String, Int]): PaymentStatus = {
    if (hasParticipants(payment, participantsBalance) && isEnoughBalance(payment, participantsBalance))
      updateBalance(payment, participantsBalance)
    else
      PaymentStatus("WARN", s"Transfer is not possible! Check the payment")
  }

  def hasParticipants(payment: ValidPayment, participantsBalance: MapState[String, Int]): Boolean =
    participantsBalance.contains(payment.from) && participantsBalance.contains(payment.recipient)

  def isEnoughBalance(payment: ValidPayment, participantsBalance: MapState[String, Int]): Boolean =
    participantsBalance.get(payment.from) >= payment.value

  def updateBalance(payment: ValidPayment, participantsBalance: MapState[String, Int]): PaymentStatus = {
    participantsBalance.put(payment.from, participantsBalance.get(payment.from) - payment.value)
    participantsBalance.put(payment.recipient, participantsBalance.get(payment.recipient) + payment.value)
    PaymentStatus("INFO", s"Payment from ${payment.from} to ${payment.recipient} completed successfully!")
  }

}
