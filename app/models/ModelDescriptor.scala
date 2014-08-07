package models

import play.api.Play.current
import java.util.Date
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class ModelDescriptor(
  collectionId: String,
  shared: Boolean = false,
  modelId: String,
  version: String,
  userId: String,
  createdBy: String,
  name: Option[String],
  dateCreated: Date,
  lastUpdated: Date
)

object ModelDescriptor {

  implicit val modelJsonWrite = new Writes[ModelDescriptor] {
    def writes(bm: ModelDescriptor): JsValue = {
      val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd")

      Json.obj(
        "collectionId" -> bm.collectionId,
        "shared" -> bm.shared,
        "modelId" -> bm.modelId,
        "version" -> bm.version,
        "userId" -> bm.userId,
        "createdBy" -> bm.createdBy,
        "name" -> bm.name,
        "dateCreated" -> sdf.format(bm.dateCreated),
        "lastUpdated" -> sdf.format(bm.lastUpdated))
    }
  }

  implicit val modelJsonRead = (
    (__ \ 'collectionId).read[String] ~
    (__ \ 'shared).read[Boolean] ~
    (__ \ 'modelId).read[String] ~
    (__ \ 'version).read[String] ~
    (__ \ 'userId).read[String] ~
    (__ \ 'createdBy).read[String] ~
    (__ \ 'name).readNullable[String] ~
    (__ \ 'dateCreated).read[Date] ~
    (__ \ 'lastUpdated).read[Date] 
   )(ModelDescriptor.apply _)
}