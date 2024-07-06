package pedro.goncalves
package pedro.goncalves.api.auth.routers

import akka.http.scaladsl.server.{Directives, Route}
import database.operations.InteractUser.getUserByUsername

import akka.http.scaladsl.model.StatusCodes
import api.controllers.auth.Service.generateToken
import api.controllers.user.Service.calculateHash

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*

import java.util.Base64
import scala.util.{Failure, Success}


class Post extends Directives with AuthCredentialsJsonSupport:

  private def decodeBase64(encoded: String): String =
    val decoder = Base64.getDecoder
    val decodedBytes = decoder.decode(encoded)
    new String(decodedBytes, "UTF-8")


  val route: Route = post {
      entity(as[AuthCredentials]) { authCred =>
        val decodedPass = decodeBase64(authCred.password)
        val hashedPass = calculateHash(decodedPass)
        val user = getUserByUsername(authCred.username)
        onComplete(user) {
          case Success(user) =>
            if (user.orNull == null)
              complete(StatusCodes.Unauthorized)
            else if (user.get.password == hashedPass)
              val token = generateToken(user.get.username, user.get.admin)
              complete(StatusCodes.OK, AuthResponse(token, java.time.LocalDateTime.now.plusHours(5).toString))
            else
              complete(StatusCodes.Unauthorized)
          case Failure(exception) => reject
        }
      }
    }
