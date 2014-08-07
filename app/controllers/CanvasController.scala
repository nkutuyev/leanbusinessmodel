package controllers

import play.api.mvc.{ Action, Controller }
import play.api.libs.json._
import play.api.Routes
import play.api.cache.Cache
import play.api.Play.current
import com.mongodb.casbah.Imports.ObjectId
import com.mongodb.casbah.WriteConcern
import models.{BusinessModel, ModelDescriptor, ModelCollection, User}
import service.Mailer
import controllers.Actions._
import securesocial.core.{Identity, Authorization}
import models.LeanSocialUser


object CanvasController extends Controller with securesocial.core.SecureSocial {
  
  val JsonOk = Ok(Json.parse("{}"))
  
  def home = SecuredAction {implicit request =>
    Ok(views.html.home(request.user.asInstanceOf[LeanSocialUser]))
  }
  
  def getModelListView = SecuredAction {implicit request =>
    Ok(views.html.modellist(Json.stringify(getAllModelObjects(request.user.identityId.userId)), request.user.asInstanceOf[LeanSocialUser]))
  }


  // *** API calls ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  def createNewCollection = SecuredAction(ajaxCall = true) { implicit request =>
    val model = ModelCollection.createNewCollection(request.user.identityId.userId)
    Ok(Json.toJson(model))
  }
  
  def createNewModel(collectionId: String) = SecuredAction(ajaxCall = true){implicit request =>
    val content = request.body.asJson.get
    val baseModelId = (content \ "modelId").as[String]
    val newVersion = (content \ "version").as[String]
    val collection = ModelCollection.findById(collectionId,request.user.identityId.userId).get
    Ok(Json.toJson(collection.createNewVersion(baseModelId, newVersion)))
  }
  
  def getModel(id: String) = SecuredAction(ajaxCall = true) {implicit request =>
    Ok(Json.toJson(BusinessModel.findById(id, request.user.identityId.userId)));
  }

  def deleteCollection(id: String) = SecuredAction(ajaxCall = true){implicit request =>
    ModelCollection.delete(id, request.user.identityId.userId)
    JsonOk
  }

  def getModelDescriptorList() = SecuredAction(ajaxCall = true){implicit request =>
    Ok(getAllModelObjects(request.user.identityId.userId))
  }
  
  def shareCollection(collectionId: String) = SecuredAction(ajaxCall = true){implicit request =>
    val content = request.body.asJson.get
    val memberId = (content \ "email").as[String]
    
    val collection = ModelCollection.findById(collectionId, request.user.identityId.userId).get
    collection.shareCollection(request.user, memberId)
    JsonOk
  }

  def getAllModelObjects(userId: String): JsValue = {
    val models: Set[ModelDescriptor] = ModelCollection.getCurrentModels(userId)
    return Json.toJson(models)
  }

  def saveCanvasField(modelId: String) = SecuredAction(ajaxCall = true){implicit request =>
    val content = request.body.asJson.get
    val fieldName = (content \ "name").as[String]
    val fieldValue = (content \ "value").as[String]
    BusinessModel.updateField(modelId, request.user.identityId.userId, fieldName, fieldValue)
    Ok
  }
  
  def getVersionList(collectionId: String) = SecuredAction(ajaxCall = true){implicit request =>
    Ok(Json.toJson(ModelCollection.getVersionList(collectionId, request.user.identityId.userId)))
  }
  
  def setCurrentVersion(collectionId: String) = SecuredAction(ajaxCall = true){implicit request =>
    val content = request.body.asJson.get
    val newCurrentVersionId = (content \ "currentVersionId").as[String]
  	ModelCollection.updateCurrentVersion(collectionId, newCurrentVersionId)
  	JsonOk
  }
  
}