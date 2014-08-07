package controllers

import play.api.mvc.{RequestHeader, Request, AnyContent}
import play.api.templates.{Html, Txt}
import play.api.{Logger, Plugin, Application}
import securesocial.core.{Identity, SecuredRequest, SocialUser}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import securesocial.controllers.Registration.RegistrationInfo
import securesocial.controllers.PasswordChange.ChangeInfo
import securesocial.controllers.TemplatesPlugin

class SecureSocialTemplatePlugin(application: Application) extends securesocial.controllers.DefaultTemplatesPlugin(application: Application) 
{
 /**
   * Returns the html for the login page
   * @param request
   * @tparam A
   * @return
   */
  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)], msg: Option[String] = None): Html =
  {
     val signupForm = Form (
	    "email" -> email.verifying(nonEmpty)
	  )
      views.html.security.login(null, form, signupForm, msg)
  }

  /**
   * Returns the html for the signup page
   *
   * @param request
   * @tparam A
   * @return
   */
  override def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html = {
    views.html.security.signup(form, token)
  }

  /**
   * Returns the html for the start signup page
   *
   * @param request
   * @tparam A
   * @return
   */
  override def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html = {
    val loginForm = Form(
	    tuple(
	      "username" -> nonEmptyText,
	      "password" -> nonEmptyText
	    )
	  )
	  views.html.security.login(null, loginForm, form, None)
  }
  
  override def getSignUpEmail(token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.security.mail.signUpEmail(token)))
  }
  
  override def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html = {
    views.html.security.resetPasswordPage(form, token)
  }

}