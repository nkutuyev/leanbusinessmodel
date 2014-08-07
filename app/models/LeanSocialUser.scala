package models

import securesocial.core._
import securesocial.core.providers._

class LeanSocialUser( firstName: String,
				      lastName: String,
				      email: Option[String],
				      val password: String,
				      val role: String) extends SocialUser(identityId = IdentityId(email.get, UsernamePasswordProvider.UsernamePassword),
													      firstName = firstName,
													      lastName = lastName,
													      fullName = s"$firstName $lastName",
													      authMethod = AuthenticationMethod.UserPassword,
													      avatarUrl = None,
													      oAuth1Info = None,
													      oAuth2Info = None,
													      email = email,
													      passwordInfo = Some(PasswordInfo("bcrypt", password, None))
													    ) {
	def isAdmin:Boolean = {
	  return this.role == "Admin"
	}
}