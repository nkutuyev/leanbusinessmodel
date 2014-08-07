package controllers

import play.api.mvc.{ Action, Controller }
import play.api.libs.json._
import play.api.Routes
import play.api.cache.Cache
import play.api.Play.current
import com.mongodb.casbah.Imports.ObjectId
import com.mongodb.casbah.WriteConcern
import models.{BusinessModel, ModelDescriptor, ModelCollection}
import controllers.Actions._
import securesocial.core.{Identity, Authorization}
import models.LeanSocialUser
import models.User

object CommunityController extends Controller with securesocial.core.SecureSocial {
  
  def getListView = SecuredAction {implicit request =>
    Ok(views.html.community(request.user.asInstanceOf[LeanSocialUser], 
    						Json.toJson(User.findCommunityMembers(request.user.email.get)).asInstanceOf[JsArray].value))
  }

}