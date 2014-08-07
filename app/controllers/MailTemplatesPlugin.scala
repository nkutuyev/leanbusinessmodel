package controllers

import play.api.mvc.{Controller, AnyContent}
import play.api.templates.{Html, Txt}
import play.api.{Logger, Plugin, Application}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._


class MailTemplatesPlugin(application: Application) extends Plugin  with Controller
{
  
  def getCollectionSharedEmail(ownerName: String, memberName: String, collectionName: String): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.security.mail.collectionSharedEmail(ownerName, memberName, collectionName)))
  }
  
  def getSharingInvitationEmail(initiator: String, token: String): (Option[Txt], Option[Html]) = {
    (None, Some(views.html.security.mail.sharingInvitationEmail(initiator, token)))
  }
 

}