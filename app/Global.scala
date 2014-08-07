import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future

object Global extends GlobalSettings {

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(BadRequest("Ooops... We're having problems, please try again later."))
  }

}