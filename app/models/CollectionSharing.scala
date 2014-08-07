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
import play.api.Logger

import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._

import mongoContext._

case class CollectionSharing(
  id: ObjectId = new ObjectId,
  collectionId: ObjectId,
  memberId: String
)

object CollectionSharing extends ModelCompanion[CollectionSharing, ObjectId] with CollectionSharingJson {

  val dao = new SalatDAO[CollectionSharing, ObjectId](collection = mongoCollection("CollectionSharing")) { }
  
  def findByMemberId(id: String): List[CollectionSharing] = {
    dao.find(MongoDBObject("memberId" -> id)).toList
  }
  
  def findByMemberAndCollectionId(collectionId: String, memberId: String): Option[CollectionSharing] = {
    dao.findOne(MongoDBObject("memberId" -> memberId, "collectionId" -> collectionId))
  }
  
  def findByCollectionId(collectionId: String): Set[CollectionSharing] = {
    dao.find(MongoDBObject("collectionId" -> collectionId)).toSet
  }

}

/**
 * Trait used to convert to and from json
 */
trait CollectionSharingJson {

  implicit val modelJsonWrite = new Writes[CollectionSharing] {
    def writes(cs: CollectionSharing): JsValue = {
      Json.obj(
        "id" -> cs.id,
        "collectionId" -> cs.collectionId,
        "memberId" -> cs.memberId)
    }
  }

  implicit val modelJsonRead = (
    (__ \ 'id).read[ObjectId].orElse(Reads.pure(new ObjectId())) ~ // generate new ObjectId if missing
    (__ \ 'collectionId).read[ObjectId] ~
    (__ \ 'memberId).read[String]
    )(CollectionSharing.apply _)
}