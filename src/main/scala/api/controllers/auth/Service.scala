package pedro.goncalves
package api.controllers.auth


import io.github.cdimascio.dotenv.Dotenv
import play.api.libs.json.*
import pdi.jwt.exceptions.{JwtEmptyAlgorithmException, JwtExpirationException}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import api.controllers.auth.exceptions.{ExpiredToken, NotValidToken}
import akka.http.scaladsl.server.directives.Credentials
import scala.util.{Failure, Success}
import java.time.Clock


object Service:

  private val dotenv = Dotenv.load
  private val jwtSecretKey = dotenv.get("SECRET_API_KEY")

  def generateToken(username: String, admin: Boolean): String =
    val now = Clock.systemUTC().instant().getEpochSecond
    val expiry = now + (5 * 3600000) / 1000

    var claim = JwtClaim(s"""{"user":"$username", "admin":$admin}""")
    claim = claim.expiresAt(expiry)

    Jwt.encode(claim, jwtSecretKey, JwtAlgorithm.HS256)


  def validateToken(token: String): Option[String] =
      Jwt.decodeRaw(token, jwtSecretKey, Seq(JwtAlgorithm.HS256)) match
      case Success(decoded) =>
        val payload = Json.parse(decoded)
        val username = (payload \ "user").as[String]
        //val profileID = (payload \ "profile").as[Int]
        Some(username)

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
