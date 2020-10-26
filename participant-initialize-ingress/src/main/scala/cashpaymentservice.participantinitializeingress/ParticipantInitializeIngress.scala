package cashpaymentservice.participantinitializeingress

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import cashpaymentservice.datamodel.ParticipantData
import cashpaymentservice.participantinitializeingress.JsonParticipant._
import cloudflow.akkastream._
import cloudflow.akkastream.util.scaladsl._
import cloudflow.streamlets._
import cloudflow.streamlets.avro._

class ParticipantInitializeIngress extends AkkaServerStreamlet {

  val participantsOut: AvroOutlet[ParticipantData] =
    AvroOutlet("participants-out").withPartitioner(RoundRobinPartitioner)

  final override def shape(): StreamletShape = StreamletShape.withOutlets(participantsOut)

  final override protected def createLogic(): AkkaStreamletLogic = HttpServerLogic.default(this, participantsOut)

}
