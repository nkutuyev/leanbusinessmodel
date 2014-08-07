package models

import play.api.Play.current
import play.api.Logger
import securesocial.core._
import securesocial.core.providers._
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import com.novus.salat.global._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._
import models.mongoContext._
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer


case class User(
  id: ObjectId,
  email: String,
  firstName: String,
  lastName: String,
  password: String,
  role: String) {

  def getIdentity(): Identity = {
    new LeanSocialUser(
      firstName = this.firstName,
      lastName = this.lastName,
      email = Some(this.email),
      password = this.password,
      role = this.role
    )
  }

}

object User extends UserDAO with UserJson {
  
  def create(i: Identity): User = {
    create(i, "User")
  }
  
  def create(i: Identity, role: String): User = {
    User(
      new ObjectId(),
      i.email.get,
      i.firstName,
      i.lastName,
      i.passwordInfo.get.password,
      role)
  }
  
}

trait UserDAO extends ModelCompanion[User, ObjectId] {

  val dao = new SalatDAO[User, ObjectId](collection = mongoCollection("User")) {}
  
  def findById(id: String): Option[Identity] = {
    val user = dao.findOne(MongoDBObject("email" -> id))
    if(user.isEmpty){
      None
    } else {
      Some(user.get.getIdentity())
    }
    
  }
  
  def findByIds(ids: List[String]) = {
    dao.find(MongoDBObject("email" -> MongoDBObject("$in" -> ids))).toList
  }
  
  def findCommunityMembers(userId: String): List[CommunityMember] = {
    
    val models: Set[ModelDescriptor] = ModelCollection.getCurrentModels(userId)
    
    val userIds: HashSet[String] = new HashSet()
    val sharedWith: HashMap[String, ListBuffer[String]] = new HashMap()
    val sharedBy: HashMap[String, ListBuffer[String]] = new HashMap()
    
    for(model <- models){
      if(model.shared){
        userIds += model.userId
        addShared(sharedBy, model.userId, model.name.get)
      } else {
        val sharings: Set[CollectionSharing] = CollectionSharing.findByCollectionId(model.collectionId)
        for(sharing <- sharings){
          userIds += sharing.memberId
          addShared(sharedWith, sharing.memberId, model.name.get)
        }
      }
    }
    
    val users = findByIds(userIds.toList)
    
    return users.map{user =>
      new CommunityMember(
    		user.firstName + " " + user.lastName,
			user.email,
			sharedWith.get(user.email).getOrElse(ListBuffer[String]()).toArray[String],
			sharedBy.get(user.email).getOrElse(ListBuffer[String]()).toArray[String]
       )
    }

  }
  
  private def addShared(sharedMap: HashMap[String, ListBuffer[String]], userId: String, modelName: String) {
    if(sharedMap.get(userId).isEmpty){
      sharedMap.put(userId, ListBuffer())
    }
    sharedMap.get(userId).get += modelName
  }

  def delete(id: String) {
    dao.remove(MongoDBObject("email" -> id))
  }

}

/**
 * Trait used to convert to and from json
 */
trait UserJson {

  implicit val tokenJsonWrite = new Writes[User] {

    def writes(user: User): JsValue = {
      Json.obj(
        "id" -> user.id,
        "email" -> user.email,
        "firstName" -> user.firstName,
        "lastName" -> user.lastName,
        "password" -> user.password,
        "role" -> user.role)
    }
  }

  implicit val tokenJsonRead = (
	    (__ \ 'id).read[ObjectId] and
	    (__ \ 'email).read[String] and
	    (__ \ 'firstName).read[String] and
	    (__ \ 'lastName).read[String] and
	    (__ \ 'password).read[String] and
	    (__ \ 'role).read[String]
    )(User.apply _)
}