package service

import securesocial.core.Identity
import play.api.{Play, Logger}
import controllers.MailTemplatesPlugin
import models.ModelCollection
import com.typesafe.plugin._
import Play.current
import play.api.libs.concurrent.Akka
import play.api.i18n.{Lang, Messages}
import play.api.templates.{Html, Txt}

/**
 * A helper class to send email notifications
 */
object Mailer {
  
  val fromAddress = current.configuration.getString("smtp.from").get


  def sendCollectionSharedNotification(user: Identity, member: Identity, collection: ModelCollection) {
    val txtAndHtml = use[MailTemplatesPlugin].getCollectionSharedEmail(user.fullName, member.fullName, collection.currentModel.name.getOrElse(""))
    sendEmail("Team member shared business model", member.email.get, txtAndHtml)
  }
  
  def sendShareingInvitation(initiator: String, email: String, token: String){
    val txtAndHtml = use[MailTemplatesPlugin].getSharingInvitationEmail(initiator, token)
    sendEmail("Team member shared business model", email, txtAndHtml)
  }

  private def sendEmail(subject: String, recipient: String, body: (Option[Txt], Option[Html])) {
    import scala.concurrent.duration._
    import play.api.libs.concurrent.Execution.Implicits._

    if ( Logger.isDebugEnabled ) {
      Logger.debug("[application] sending email to %s".format(recipient))
      Logger.debug("[application] mail = [%s]".format(body))
    }

    Akka.system.scheduler.scheduleOnce(1.seconds) {
      val mail = use[MailerPlugin].email
      mail.setSubject(subject)
      mail.setRecipient(recipient)
      mail.setFrom(fromAddress)
      // the mailer plugin handles null / empty string gracefully
      mail.send(body._1.map(_.body).getOrElse(""), body._2.map(_.body).getOrElse(""))
    }
  }
}