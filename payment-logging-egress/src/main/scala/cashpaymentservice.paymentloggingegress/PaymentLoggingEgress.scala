package cashpaymentservice.paymentloggingegress

import akka.event.LoggingAdapter
import akka.stream.scaladsl.{RunnableGraph, Sink}
import cashpaymentservice.datamodel.{MessageType, PaymentStatus}
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.akkastream.util.scaladsl.Merger
import cloudflow.akkastream.{AkkaStreamlet, AkkaStreamletLogic}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet

class PaymentLoggingEgress extends AkkaStreamlet {

  val checkStatusIn: AvroInlet[PaymentStatus]   = AvroInlet("check-status-in")
  val processStatusIn: AvroInlet[PaymentStatus] = AvroInlet("process-status-in")

  override def shape(): StreamletShape = StreamletShape.withInlets(checkStatusIn, processStatusIn)

  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic() {

    override def runnableGraph(): RunnableGraph[_] = {

      val log: LoggingAdapter = system.log

      Merger
        .source(checkStatusIn, processStatusIn)
        .map(s =>
          s.infoType match {
            case MessageType.WARN => log.warning(s.message)
            case MessageType.INFO => log.info(s.message)
          }
        )
        .to(Sink.ignore)
    }
  }

}
