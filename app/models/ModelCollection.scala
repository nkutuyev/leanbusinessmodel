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
import securesocial.core.{Identity}
import mongoContext._
import service.{Mailer, MongoUserService}

case class ModelCollection(
	  id: ObjectId = new ObjectId,
	  userId: String,
	  dateCreated: Date = new Date(),
	  currentVersionId: String
) {
  
  def shareCollection(initiator: Identity, targetMemberId: String){
    val sharing = CollectionSharing(collectionId = id, memberId = targetMemberId)
    CollectionSharing.insert(sharing)
    // handle member notification
    val targetMember = User.findById(targetMemberId)
    if(targetMember.isEmpty){
      // send invitation to join
      val token = MongoUserService.createToken(targetMemberId, isSignUp = true)
      Mailer.sendShareingInvitation(initiator.fullName, targetMemberId, token.uuid)
    } else {
      // send sharing notification
      val owner = User.findById(userId)
      Mailer.sendCollectionSharedNotification(initiator, targetMember.get, this)
    }
  }
  
  def descriptor: ModelDescriptor = {
    descriptor(false)
  }
  
  def descriptor(shared: Boolean): ModelDescriptor = {
	 val model = currentModel
	 createDescriptor(model, shared)
  }
  
  def createDescriptor(model: BusinessModel): ModelDescriptor = {
    createDescriptor(model, false)
  }
  
  def createDescriptor(model: BusinessModel, isShared: Boolean): ModelDescriptor = {
    val owner = User.findById(userId)
    ModelDescriptor(
	  collectionId = id.toString,
	  shared = isShared,
	  modelId = model.id.toString,
	  version = model.version,
	  userId = this.userId,
	  createdBy = owner.get.fullName,
	  name = model.name,
	  dateCreated = this.dateCreated,
	  lastUpdated = model.lastUpdated
	)
  }
  
  def currentModel: BusinessModel = {
    BusinessModel.findById(currentVersionId, userId).get
  }
  
  def createNewVersion(baseModelId: String, newVersion: String): ModelDescriptor = {
    val baseModel = BusinessModel.findOne(MongoDBObject("_id" -> new ObjectId(baseModelId), "collectionId" -> id)).get
    val newModel = baseModel.copy(id = new ObjectId(), version = newVersion)
    BusinessModel.insert(newModel)
    val updatedCollection = this.copy(currentVersionId = newModel.id.toString)
    ModelCollection.save(updatedCollection)
    return updatedCollection.createDescriptor(newModel)
  }
}

object ModelCollection extends ModelCollectionDAO with ModelCollectionJson

trait ModelCollectionDAO extends ModelCompanion[ModelCollection, ObjectId] {

  val dao = new SalatDAO[ModelCollection, ObjectId](collection = mongoCollection("ModelCollection")) {
    val models = new ChildCollection[BusinessModel, ObjectId](collection = mongoCollection("BusinessModel"),parentIdField = "collectionId") {}
    val versions = new ChildCollection[ModelVersion, ObjectId](collection = mongoCollection("BusinessModel"),parentIdField = "collectionId") {}  
    val sharings = new ChildCollection[CollectionSharing, ObjectId](collection = mongoCollection("CollectionSharing"),parentIdField = "collectionId") {}   
  }
  
  def findById(id: String, userId: String): Option[ModelCollection] = {
    val collection = dao.findOne(MongoDBObject("_id" -> new ObjectId(id)))
    if(collection.get.userId == userId){
      return collection
    }
    // check sharing
    val sharing = CollectionSharing.findByMemberAndCollectionId(collection.get.id.toString, userId)
    if(sharing.isEmpty){
      return None
    } else {
      return collection
    }
  }
  
  private def findById(id: String): Option[ModelCollection] = {
    findById(new ObjectId(id))
  }
  
  private def findById(id: ObjectId): Option[ModelCollection] = {
    dao.findOne(MongoDBObject("_id" -> id))
  }
  
  def createNewCollection(currentUserId: String): ModelDescriptor = {
    val modelId = new ObjectId
    val collection = new ModelCollection(userId = currentUserId, currentVersionId = modelId.toString)
    dao.insert(collection, WriteConcern.Safe)
    val model = new BusinessModel(id = modelId, collectionId = collection.id, version = "v.1.0", userId = currentUserId)
    dao.models.insert(model, WriteConcern.Safe)
    return collection.createDescriptor(model)
  }
  
  def getCurrentModels(currentUserId: String): Set[ModelDescriptor] = {
    val collections = dao.find(MongoDBObject("userId" -> currentUserId)).toSet
    val descriptors = collections.map{(collection: ModelCollection) =>
      collection.descriptor
    }
    // add shared collections
    val sharings = CollectionSharing.findByMemberId(currentUserId)
    val sharedDescriptors = sharings.map{sharing: CollectionSharing =>
        val collection = findById(sharing.collectionId)
        collection match {
		  case None => null
		  case Some(x) => x.descriptor(true)
		}
    }
    return descriptors ++ sharedDescriptors
  }
  
  def getVersionList(collectionId: String, userId: String): List[ModelVersion] = {
    dao.versions.findByParentId(new ObjectId(collectionId)).toList
  }
  
  def delete(id: String, userId: String) {
    val collection = dao.findOne(MongoDBObject("_id" -> new ObjectId(id)))
    if(collection.get.userId != userId){
	    // check sharing
	    val sharing = CollectionSharing.findByMemberAndCollectionId(collection.get.id.toString, userId)
	    if(sharing.isEmpty){
	      return
	    }
    }
    val collectionId = collection.get.id
    dao.remove(MongoDBObject("_id" -> collectionId))
    dao.models.removeByParentId(collectionId)
    dao.sharings.removeByParentId(collectionId)
  }
  
  def updateCurrentVersion(collectionId: String, modelId: String) {
    dao.update(MongoDBObject("_id" -> new ObjectId(collectionId)), $set("currentVersionId" -> modelId), false, false)
  }

}

/**
 * Trait used to convert to and from json
 */
trait ModelCollectionJson {
  
  val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd")

  implicit val modelJsonWrite = new Writes[ModelCollection] {
    def writes(mc: ModelCollection): JsValue = {
      Json.obj(
        "id" -> mc.id,
        "userId" -> mc.userId,
        "dateCreated" -> sdf.format(mc.dateCreated),
        "currentVersionId" -> mc.currentVersionId)
    }
  }

  implicit val modelJsonRead = (
    (__ \ 'id).read[ObjectId].orElse(Reads.pure(new ObjectId())) ~ // generate new ObjectId if missing
    (__ \ 'userId).read[String] ~
    (__ \ 'dateCreated).read[Date].orElse(Reads.pure(new Date())) ~
    (__ \ 'currentVersionId).read[String]
    )(ModelCollection.apply _)
}