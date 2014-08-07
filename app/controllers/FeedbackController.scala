package controllers

import play.api.mvc.{ Action, Controller }
import play.api.libs.json._
import play.api.Routes
import play.api.cache.Cache
import play.api.Play.current
import com.mongodb.casbah.Imports.ObjectId
import com.mongodb.casbah.WriteConcern
import models.{Feedback, LeanSocialUser}
import service.Mailer
import controllers.Actions._
import securesocial.core.{Identity, Authorization}

object FeedbackController extends Controller with securesocial.core.SecureSocial {
  
  val JsonOk = Ok(Json.parse("{}"))

  def saveFeedback = SecuredAction(ajaxCall = true) {implicit request =>
    val content = request.body.asJson.get
    val value = (content \ "feedback").as[String]
    val sourceUrl = (content \ "url").as[String]
    val feedbackObject = Feedback(feedback = value, userId = request.user.identityId.userId, url = sourceUrl)
    Feedback.insert(feedbackObject)
    JsonOk
  }
  
    def showFeedbackView = SecuredAction(InRole("Admin")) {implicit request =>
	    Ok(views.html.feedback(request.user.asInstanceOf[LeanSocialUser], Json.toJson(Feedback.getFeedbackList(100)).asInstanceOf[JsArray].value))
	}
    
    def getFeedbackList = SecuredAction(ajaxCall = true, InRole("Admin")) {implicit request =>
	    Ok(Json.toJson(Feedback.getFeedbackList(100)))
	}

}