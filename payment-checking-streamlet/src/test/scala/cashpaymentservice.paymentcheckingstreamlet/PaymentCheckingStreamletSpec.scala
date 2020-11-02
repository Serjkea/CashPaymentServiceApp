package cashpaymentservice.paymentcheckingstreamlet

import cashpaymentservice.datamodel.{PaymentData, PaymentStatus, ValidPayment}
import cloudflow.flink.testkit._
import org.apache.flink.api.scala.createTypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class PaymentCheckingStreamletSpec extends FlinkTestkit with WordSpecLike with Matchers with BeforeAndAfterAll {

  "A PaymentCheckingStreamlet" should {
    "checking input payments" in {

      @transient lazy val env = StreamExecutionEnvironment.getExecutionEnvironment

      val paymentCheckingStreamlet = new PaymentCheckingStreamlet

      val paymentData = List(PaymentData("<name1> -> <name2>: <2000>"))//, PaymentData("<name3> -> <name4>: <1000>"))

      val paymentsIn: FlinkInletTap[PaymentData] = inletAsTap[PaymentData](paymentCheckingStreamlet.paymentsIn, env.addSource(FlinkSource.CollectionSourceFunction(paymentData)))

      val validPaymentsOut: FlinkOutletTap[ValidPayment] = outletAsTap[ValidPayment](paymentCheckingStreamlet.validPaymentsOut)
      val checkStatusOut: FlinkOutletTap[PaymentStatus] = outletAsTap[PaymentStatus](paymentCheckingStreamlet.checkStatusOut)

      run(paymentCheckingStreamlet, Seq(paymentsIn), Seq(validPaymentsOut, checkStatusOut), env)

      TestFlinkStreamletContext.result should contain(ValidPayment("payment", "name1", "name2", 2000).toString)

    }
  }

}
