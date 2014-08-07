package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import com.novus.salat.global._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._

import mongoContext._

case class BusinessModel(
  id: ObjectId = new ObjectId,
  collectionId: ObjectId,
  version: String,
  userId: String,
  name: Option[String] = None,
  dateCreated: Date = new Date(),
  lastUpdated: Date = new Date(),
  problem: Option[String] = None,
  existingAlternatives: Option[String] = None,
  solution: Option[String] = None,
  keyMetrics: Option[String] = None,
  valueProposition: Option[String] = None,
  highLevelConcept: Option[String] = None,
  unfairAdvantage: Option[String] = None,
  channels: Option[String] = None,
  customerSegments: Option[String] = None,
  earlyAdopters: Option[String] = None,
  costStructure: Option[String] = None,
  revenueStreams: Option[String] = None
)

object BusinessModel extends BusinessModelDAO with BusinessModelJson

trait BusinessModelDAO extends ModelCompanion[BusinessModel, ObjectId] {

  val dao = new SalatDAO[BusinessModel, ObjectId](collection = mongoCollection("BusinessModel")) {}

  def getAllModels(userId: String): List[BusinessModel] = find(MongoDBObject("userId" -> userId)).toList

  def findById(id: String, userId: String): Option[BusinessModel] = {
    val model = dao.findOne(MongoDBObject("_id" -> new ObjectId(id)))
    if(model.get.userId == userId){
      return model
    }
    // check sharing
    val sharing = CollectionSharing.findByMemberAndCollectionId(memberId=userId, collectionId = model.get.collectionId.toString)
    if(sharing.isEmpty){
      return None
    } else {
      return model
    }
  }
  
  def updateField(id: String, userId: String, fieldName: String, fieldValue: String){
    val model = findById(id, userId)
    if(!model.isEmpty){
    	dao.update(MongoDBObject("_id" -> new ObjectId(id), "userId" -> model.get.userId), $set(fieldName -> fieldValue, "lastUpdated" -> new Date()), false, false)
    }
  }

}

/**
 * Trait used to convert to and from json
 */
trait BusinessModelJson {

  implicit val modelJsonWrite = new Writes[BusinessModel] {
    def writes(bm: BusinessModel): JsValue = {
      val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd")

      Json.obj(
        "id" -> bm.id,
        "collectionId" -> bm.collectionId,
        "version" -> bm.version,
        "userId" -> bm.id,
        "name" -> bm.name,
        "dateCreated" -> sdf.format(bm.dateCreated),
        "lastUpdated" -> sdf.format(bm.lastUpdated),
        "problem" -> bm.problem,
        "existingAlternatives" -> bm.existingAlternatives,
        "solution" -> bm.solution,
        "keyMetrics" -> bm.keyMetrics,
        "valueProposition" -> bm.valueProposition,
        "highLevelConcept" -> bm.highLevelConcept,
        "unfairAdvantage" -> bm.unfairAdvantage,
        "channels" -> bm.channels,
        "customerSegments" -> bm.customerSegments,
        "earlyAdopters" -> bm.earlyAdopters,
        "costStructure" -> bm.costStructure,
        "revenueStreams" -> bm.revenueStreams)
    }
  }

  implicit val modelJsonRead = (
    (__ \ 'id).read[ObjectId].orElse(Reads.pure(new ObjectId())) ~
    (__ \ 'collectionId).read[ObjectId] ~
    (__ \ 'version).read[String] ~
    (__ \ 'userId).read[String] ~
    (__ \ 'name).readNullable[String] ~
    (__ \ 'dateCreated).read[Date].orElse(Reads.pure(new Date())) ~
    (__ \ 'lastUpdated).read[Date].orElse(Reads.pure(new Date())) ~
    (__ \ 'problem).readNullable[String] ~
    (__ \ 'existingAlternatives).readNullable[String] ~
    (__ \ 'solution).readNullable[String] ~
    (__ \ 'keyMetrics).readNullable[String] ~
    (__ \ 'valueProposition).readNullable[String] ~
    (__ \ 'highLevelConcept).readNullable[String] ~
    (__ \ 'unfairAdvantage).readNullable[String] ~
    (__ \ 'channels).readNullable[String] ~
    (__ \ 'customerSegments).readNullable[String] ~
    (__ \ 'earlyAdopters).readNullable[String] ~
    (__ \ 'costStructure).readNullable[String] ~
    (__ \ 'revenueStreams).readNullable[String]
    )(BusinessModel.apply _)
}