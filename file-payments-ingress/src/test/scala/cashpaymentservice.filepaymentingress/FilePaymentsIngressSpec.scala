package cashpaymentservice.filepaymentingress

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import cashpaymentservice.datamodel.PaymentData
import cloudflow.akkastream.testkit.scaladsl.AkkaStreamletTestKit
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}

class FilePaymentsIngressSpec extends WordSpec with MustMatchers with BeforeAndAfterAll{

  private implicit val system = ActorSystem("AkkaStreamletSpec")
  private implicit val mat = ActorMaterializer

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A FilePaymentsIngress" should {

    val testkit = AkkaStreamletTestKit(system)

    "Takes strings from file" in {
      val expectedData = Vector(PaymentData("<Mike01> -> <Bob01>: <1000>"))
      val fileIngress = new FilePaymentsIngress
      val out = testkit.outletAsTap(fileIngress.paymentsOut)

      testkit.run(fileIngress, out, () => {
        out.probe.receiveN(1) mustBe expectedData.map(d => fileIngress.paymentsOut.partitioner(d) -> d)
      })

    }
  }



}
