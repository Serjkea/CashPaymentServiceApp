package cashpaymentservice.paymentloggingegress

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.testkit.TestKit
import cashpaymentservice.datamodel.PaymentStatus
import cloudflow.akkastream.testkit.scaladsl.AkkaStreamletTestKit
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}

class PaymentLoggingEgressSpec extends WordSpec with MustMatchers with BeforeAndAfterAll{

  private implicit val system = ActorSystem("AkkaStreamletSpec")
  private implicit val mat = ActorMaterializer

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A PaymentLoggingEgress" should {

    val testKit = AkkaStreamletTestKit(system)

    "Logging messages" in {
      val paymentStatusInfo = PaymentStatus("INFO", "Test Info message")
      val paymentStatusWarn = PaymentStatus("WARN", "Test Warning message")

      val source = Source(List(paymentStatusInfo,paymentStatusWarn))

      val logEgress = new PaymentLoggingEgress
      val checkStatusIn = testKit.inletFromSource(logEgress.checkStatusIn,source)
      val processStatusIn = testKit.inletFromSource(logEgress.processStatusIn,source)

      testKit.run(logEgress,List(checkStatusIn,processStatusIn),List(),() => {
        1 mustBe 1
      })

    }
  }

}
