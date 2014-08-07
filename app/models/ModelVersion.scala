package models

import play.api.Play.current
import com.mongodb.casbah.Imports._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._

import mongoContext._

case class ModelVersion(
  id: ObjectId,
  collectionId: ObjectId,
  version: String)

object ModelVersion {

  implicit val modelJsonWrite = new Writes[ModelVersion] {
    def writes(mv: ModelVersion): JsValue = {
      Json.obj(
        "id" -> mv.id,
        "collectionId" -> mv.collectionId,
        "version" -> mv.version)
    }
  }
  
  implicit val modelJsonRead = (
	(__ \ 'id).read[ObjectId] ~
    (__ \ 'collectionId).read[ObjectId] ~
    (__ \ 'version).read[String]
  )(ModelVersion.apply _)
  
}