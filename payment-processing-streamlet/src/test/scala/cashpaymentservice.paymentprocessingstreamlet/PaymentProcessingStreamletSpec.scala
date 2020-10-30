package cashpaymentservice.paymentprocessingstreamlet

import cashpaymentservice.datamodel.{ParticipantData, PaymentStatus, ValidPayment}
import cloudflow.flink.testkit._
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class PaymentProcessingStreamletSpec extends FlinkTestkit with WordSpecLike with Matchers with BeforeAndAfterAll {

  "A PaymentProcessingStreamlet" should {

    @transient lazy val env = StreamExecutionEnvironment.getExecutionEnvironment
    val paymentProcessingStreamlet = new PaymentProcessingStreamlet

    "Send info message about updated balance" in {

      val participantsData = List(ParticipantData("payment", "name1", 1000), ParticipantData("payment", "name2", 2000))
      val validPaymentsData = List(ValidPayment(" ", " ", " ", 0))
      val participantsIn: FlinkInletTap[ParticipantData] = inletAsTap[ParticipantData](paymentProcessingStreamlet.participantsIn, env.addSource(FlinkSource.CollectionSourceFunction(participantsData)))
      val validPaymentsIn: FlinkInletTap[ValidPayment] = inletAsTap[ValidPayment](paymentProcessingStreamlet.validPaymentsIn, env.addSource(FlinkSource.CollectionSourceFunction(validPaymentsData)))
      val paymentStatusOut: FlinkOutletTap[PaymentStatus] = outletAsTap[PaymentStatus](paymentProcessingStreamlet.processStatusOut)

      run(paymentProcessingStreamlet, Seq(participantsIn, validPaymentsIn), Seq(paymentStatusOut), env)

      TestFlinkStreamletContext.result should contain (PaymentStatus("INFO", "For participant: name1 - balance updated!").toString)
      TestFlinkStreamletContext.result should contain (PaymentStatus("INFO", "For participant: name2 - balance updated!").toString)
    }

    "Send info message about succesfully completed payment" in {

      val participantsData = List(ParticipantData("payment", "name1", 1000), ParticipantData("payment", "name2", 2000))
      val validPaymentsData = List(ValidPayment("payment", "name1", "name2", 100))
      val participantsIn: FlinkInletTap[ParticipantData] = inletAsTap[ParticipantData](paymentProcessingStreamlet.participantsIn, env.addSource(FlinkSource.CollectionSourceFunction(participantsData)))
      val validPaymentsIn: FlinkInletTap[ValidPayment] = inletAsTap[ValidPayment](paymentProcessingStreamlet.validPaymentsIn, env.addSource(FlinkSource.CollectionSourceFunction(validPaymentsData)))
      val paymentStatusOut: FlinkOutletTap[PaymentStatus] = outletAsTap[PaymentStatus](paymentProcessingStreamlet.processStatusOut)

      run(paymentProcessingStreamlet, Seq(participantsIn, validPaymentsIn), Seq(paymentStatusOut), env)

      TestFlinkStreamletContext.result should contain (PaymentStatus("INFO", "Payment from name1 to name2 completed successfully!").toString)
    }

  }

}
