package service

import _root_.java.util.UUID
import play.api.{Logger, Application}
import securesocial.core._
import securesocial.core.providers._
import models.{User, SecurityToken}
import org.joda.time.DateTime
import play.api.Play

/**
 * A MongoDB user service in Scala
 *
 */
class MongoUserService(application: Application) extends UserServicePlugin(application) {

  def find(id: IdentityId): Option[Identity] = {
    val user = User.findById(id.userId)
    return user
  }

  def findByEmailAndProvider(email: String, providerId: String): Option[Identity] = {
    User.findById(email)
  }

  def save(user: Identity): Identity = {
	User.findById(user.email.get) match {
	  case None => User.save(User.create(user))
	  case Some(x) => // noop
	}
    user
  }

  def save(token: Token) {
    SecurityToken.save(SecurityToken.create(token))
  }

  def findToken(token: String): Option[Token] = {
    SecurityToken.findTokenById(token)
  }

  def deleteToken(uuid: String) {
    SecurityToken.delete(uuid)
  }


  def deleteExpiredTokens() {
    SecurityToken.deleteExpiredTokens
  }

}

object MongoUserService {
  
  val TokenDurationKey = "securesocial.userpass.tokenDuration"
  val DefaultDuration = 60
  val TokenDuration = Play.current.configuration.getInt(TokenDurationKey).getOrElse(DefaultDuration)
  
  def createToken(email: String, isSignUp: Boolean): Token = {
    val uuid = UUID.randomUUID().toString
    val now = DateTime.now

    val token = Token(
      uuid, email,
      now,
      now.plusMinutes(TokenDuration),
      isSignUp = isSignUp
    )
    UserService.save(token)
    return token
  }
}
