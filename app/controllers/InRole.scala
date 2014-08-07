package controllers

import securesocial.core.{Authorization, Identity}
import models.LeanSocialUser

case class InRole(role: String) extends Authorization {
  def isAuthorized(user: Identity) = {
    user.asInstanceOf[LeanSocialUser].role == role
  }
}
