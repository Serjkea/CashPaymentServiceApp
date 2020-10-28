package cashpaymentservice.participantinitializeingress

import cashpaymentservice.datamodel.ParticipantData
import spray.json._

case object JsonParticipant extends DefaultJsonProtocol {
  implicit val participantFormat: RootJsonFormat[ParticipantData] =
    jsonFormat(ParticipantData.apply, "opType", "nameId", "balance")
}
