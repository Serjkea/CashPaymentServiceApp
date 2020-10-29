package cashpaymentservice.paymentprocessingstreamlet

import cashpaymentservice.datamodel.{ParticipantData, PaymentStatus, ValidPayment}
import org.apache.flink.api.common.state.{MapState, MapStateDescriptor}
import org.apache.flink.streaming.api.functions.co.KeyedCoProcessFunction
import org.apache.flink.util.Collector

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
