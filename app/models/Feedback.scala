package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import com.novus.salat.global._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.Logger

import se.radley.plugin.salat._
import se.radley.plugin.salat.Binders._

import models.mongoContext._

case class Feedback(
  id: ObjectId = new ObjectId,
  feedback: String,
  userId: String,
  url: String,
  dateSubmitted: Date = new Date()
)

object Feedback extends ModelCompanion[Feedback, ObjectId] with FeedbackJson {

  val dao = new SalatDAO[Feedback, ObjectId](collection = mongoCollection("Feedback")) { }
  
  def getFeedbackList(maxSize: Int): List[Feedback] = {
    dao.find(MongoDBObject.empty)
    	.sort(orderBy = MongoDBObject("_id" -> -1))
    	.limit(maxSize).toList
  }

}

/**
 * Trait used to convert to and from json
 */
trait FeedbackJson {
  
  val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd")

  implicit val modelJsonWrite = new Writes[Feedback] {
    def writes(feedback: Feedback): JsValue = {
      Json.obj(
        "id" -> feedback.id,
        "feedback" -> feedback.feedback,
        "userId" -> feedback.userId,
        "url" -> feedback.url,
        "dateSubmitted" -> sdf.format(feedback.dateSubmitted)
      )
    }
  }

  implicit val modelJsonRead = (
    (__ \ 'id).read[ObjectId] ~
    (__ \ 'feedback).read[String] ~
    (__ \ 'userId).read[String] ~
    (__ \ 'url).read[String] ~
    (__ \ 'dateSubmitted).read[Date]
    )(Feedback.apply _)
}