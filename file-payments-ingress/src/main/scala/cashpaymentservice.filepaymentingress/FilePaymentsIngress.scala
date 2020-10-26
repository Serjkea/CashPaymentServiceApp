package cashpaymentservice.filepaymentingress

import java.nio.file.Path

import akka.NotUsed
import akka.stream.scaladsl.{ FileIO, Framing, RunnableGraph, Source }
import akka.util.ByteString
import cashpaymentservice.datamodel.PaymentData
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.akkastream.{ AkkaStreamlet, AkkaStreamletLogic }
import cloudflow.streamlets.avro.AvroOutlet
import cloudflow.streamlets.{ RoundRobinPartitioner, StreamletShape, StringConfigParameter }
import scala.concurrent.duration._

class FilePaymentsIngress extends AkkaStreamlet {

  val paymentsOut: AvroOutlet[PaymentData] =
    AvroOutlet[PaymentData]("payments-out").withPartitioner(RoundRobinPartitioner)

  override def shape(): StreamletShape = StreamletShape.withOutlets(paymentsOut)

  val fileNameConf  = StringConfigParameter("filename", "payments filename", Some("payments.dat"))
  val directoryConf = StringConfigParameter("directory", "payments directory", Some("./test-data/"))

  override def configParameters: Vector[StringConfigParameter] = Vector(fileNameConf, directoryConf)

  override protected def createLogic(): AkkaStreamletLogic = new RunnableGraphStreamletLogic() {

    override def runnableGraph(): RunnableGraph[_] = {

      val fileName  = fileNameConf.value
      val directory = directoryConf.value

      Source
        .tick(1 minute, 5 minutes, NotUsed)
        //        .single(NotUsed)
        .flatMapConcat(_ => FileIO.fromPath(Path.of(s"$directory$fileName")))
        .via(Framing.delimiter(ByteString("\n"), Int.MaxValue))
        .map(bs => {
          println(bs.utf8String)
          PaymentData(bs.utf8String)
        })
        .to(plainSink(paymentsOut))
    }

  }

}
