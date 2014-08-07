package models

import play.api.libs.json.Writes
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class CommunityMember (
	name: String,
	email: String,
	sharedWith: Array[String],
	sharedBy: Array[String]
)

object CommunityMember{
  
  implicit val modelJsonWrite = new Writes[CommunityMember] {
    def writes(member: CommunityMember): JsValue = {
      Json.obj(
        "name" -> member.name,
        "email" -> member.email,
        "sharedWith" -> member.sharedWith,
        "sharedBy" -> member.sharedBy
      )
    }
  }

  implicit val modelJsonRead: Reads[CommunityMember] = (
	    (__ \ 'name).read[String] ~
	    (__ \ 'email).read[String] ~
	    (__ \ 'sharedWith).read[Array[String]] ~
	    (__ \ 'sharedBy).read[Array[String]]
  )(CommunityMember.apply _)
    
}