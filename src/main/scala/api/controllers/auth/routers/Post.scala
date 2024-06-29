package pedro.goncalves
package api.controllers.auth.routers


import akka.http.scaladsl.server.{Directives, Route}
import api.controllers.auth.{AuthCredentials, AuthCredentialsJsonSupport}
import database.operations.InteractUser.getUserByUsername
import java.util.Base64
import scala.util.Success


class Post extends Directives with AuthCredentialsJsonSupport {

  private def decodeBase64(encoded: String): String =
    val decoder = Base64.getDecoder
    val decodedBytes = decoder.decode(encoded)
    new String(decodedBytes, "UTF-8")

  val route: Route = post {
    entity(as[AuthCredentials]) { authCred =>
      val decodedPass = decodeBase64(authCred.password)
      val user = getUserByUsername(authCred.username)
      onComplete(user) {
        case Success(user) =>
          complete("ok")
      }
    }
  }
}
