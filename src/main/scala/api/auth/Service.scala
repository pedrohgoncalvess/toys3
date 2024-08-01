package pedro.goncalves
package api.auth


import scala.util.{Failure, Success}
import java.time.Clock

import io.github.cdimascio.dotenv.Dotenv
import play.api.libs.json.*
import pdi.jwt.exceptions.{JwtEmptyAlgorithmException, JwtExpirationException}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import akka.http.scaladsl.server.directives.Credentials

import api.auth.exceptions.{ExpiredToken, NotValidToken}
import database.models.User


object Service:

  private val dotenv = Dotenv.load
  private val jwtSecretKey = dotenv.get("SECRET_API_KEY")

  def generateToken(user:User): String =
    val now = Clock.systemUTC().instant().getEpochSecond
    val expiry = now + (5 * 3600000) / 1000

    var claim = JwtClaim(s"""{"user_id":"${user.id}", "admin":${user.admin}}""")
    claim = claim.expiresAt(expiry)

    Jwt.encode(claim, jwtSecretKey, JwtAlgorithm.HS256)


  private def validateToken(token: String): Option[String] =
      Jwt.decodeRaw(token, jwtSecretKey, Seq(JwtAlgorithm.HS256)) match
      case Success(decoded) =>
        val payload = Json.parse(decoded)
        val userID = (payload \ "user_id").as[String]
        //val profileID = (payload \ "profile").as[Int]
        Some(userID)

      case Failure(_: JwtExpirationException) => throw ExpiredToken()
      case Failure(_: JwtEmptyAlgorithmException) => throw NotValidToken()
      case Failure(exception) => None

  def endpointAuthenticator(credentials: Credentials): Option[String] =
    credentials match {
      case p@Credentials.Provided(token) =>
       validateToken(token)
      case _ =>
        None
    }
