package models

import play.api.Play.current
import securesocial.core._
import securesocial.core.providers._
import securesocial.core.providers.Token
import org.joda.time.DateTime

import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import com.novus.salat.global._
import play.api.libs.functional.syntax._
import play.api.libs.json._

import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._

import mongoContext._

case class SecurityToken(id: ObjectId, tokenId: String, email: String, creationTime: Long, expirationTime: Long, isSignUp: Boolean){
  def getToken: Token = {
    Token(
    	tokenId,
    	email,
    	new DateTime(creationTime),
    	new DateTime(expirationTime),
    	isSignUp
    )
  }
}

object SecurityToken extends ModelCompanion[SecurityToken, String] with SecurityTokenJson {
  
  def create(token: Token): SecurityToken = {
    SecurityToken(
        new ObjectId(),
        token.uuid,
        token.email,
        token.creationTime.getMillis(),
        token.expirationTime.getMillis(),
        token.isSignUp
    )
  }

  val dao = new SalatDAO[SecurityToken, String](collection = mongoCollection("SecurityToken")) {}

  def delete(id: String) {
    dao.remove(MongoDBObject("tokenId" -> id))
  }
  
  def findTokenById(id: String): Option[Token] = {
    val securityToken = dao.findOne(MongoDBObject("tokenId" -> id))
    if(securityToken.isEmpty){
      return None
    }else{
      return Some(securityToken.get.getToken)
    }
  }
  
  def deleteExpiredTokens = {
    val currentTime = System.currentTimeMillis();
    dao.remove(MongoDBObject("expirationTime" -> MongoDBObject("$lt" -> currentTime)))
  }

}

/**
 * Trait used to convert to and from json
 */
trait SecurityTokenJson {

  implicit val tokenJsonWrite = new Writes[SecurityToken] {
    def writes(token: SecurityToken): JsValue = {
      Json.obj(
        "id" -> token.id,
        "tokenId" -> token.tokenId,
        "email" -> token.email,
        "creationTime" -> token.creationTime,
        "expirationTime" -> token.expirationTime,
        "isSignUp" -> token.isSignUp
      )
    }
  }

  implicit val tokenJsonRead = (
    (__ \ 'id).read[ObjectId] and
    (__ \ 'tokenId).read[String] and
    (__ \ 'email).read[String] and
    (__ \ 'creationTime).read[Long] and
    (__ \ 'expirationTime).read[Long] and
    (__ \ 'isSignUp).read[Boolean] 
   )(SecurityToken.apply _)
}