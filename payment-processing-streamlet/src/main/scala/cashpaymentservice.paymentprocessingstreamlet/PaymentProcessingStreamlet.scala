package cashpaymentservice.paymentprocessingstreamlet

import cashpaymentservice.datamodel.{ ParticipantData, PaymentStatus, ValidPayment }
import cloudflow.flink._
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.{ AvroInlet, AvroOutlet }
import org.apache.flink.api.common.state.{ ValueState, ValueStateDescriptor }
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
          .keyBy(pd => pd.nameId, vp => vp.from)
          .process(new MakingPayment)

      writeStream(processStatusOut, outputPaymentStatus)
    }
  }
}

class MakingPayment extends KeyedCoProcessFunction[String, ParticipantData, ValidPayment, PaymentStatus] {

  @transient lazy val stateDescriptor               = new ValueStateDescriptor[Int]("balanceState", classOf[Int])
  @transient lazy val balanceState: ValueState[Int] = getRuntimeContext.getState(stateDescriptor)

  def processElement1(
    participant: ParticipantData,
    ctx: KeyedCoProcessFunction[String, ParticipantData, ValidPayment, PaymentStatus]#Context,
    out: Collector[PaymentStatus]
  ): Unit = {
    balanceState.update(participant.balance)
    out.collect(PaymentStatus("INFO", s"For participant: ${participant.nameId} - balance updated!"))
  }

  def processElement2(
    payment: ValidPayment,
    ctx: KeyedCoProcessFunction[String, ParticipantData, ValidPayment, PaymentStatus]#Context,
    out: Collector[PaymentStatus]
  ): Unit = {
    out.collect(makePayment(payment, balanceState))
  }

  def makePayment(payment: ValidPayment, balance: ValueState[Int]): PaymentStatus = {
    if (hasParticipant(payment, balance) && isEnoughBalance(payment, balance))
      updateBalance(payment, balance)
    else
      PaymentStatus("WARN", s"Transfer is not possible! Check the payment")
  }

  def hasParticipant(payment: ValidPayment, balance: ValueState[Int]): Boolean = balance != null //TODO!!!

  def isEnoughBalance(payment: ValidPayment, balance: ValueState[Int]): Boolean = balance.value() >= payment.value

  def updateBalance(payment: ValidPayment, balance: ValueState[Int]): PaymentStatus = {
    balance.update(balance.value() - payment.value)
    PaymentStatus("INFO", s"Payment from ${payment.from} to ${payment.recipient} completed successfully!")
  }

}
