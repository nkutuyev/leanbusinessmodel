package controllers

import play.api.mvc._
import play.api.libs.json.Json
import play.api.Routes

object MainController extends Controller {

  def redirectToLogin = Action { implicit request =>
    Redirect("/login")
  }

}