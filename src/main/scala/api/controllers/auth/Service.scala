package pedro.goncalves
package api.controllers.auth


import io.github.cdimascio.dotenv.Dotenv
import play.api.libs.json.*
import pdi.jwt.exceptions.{JwtEmptyAlgorithmException, JwtExpirationException}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import api.controllers.auth.exceptions.{ExpiredToken, NotValidToken}
import scala.util.{Failure, Success}
import java.time.Clock


object Service:

  private val dotenv = Dotenv.load
  private val jwtSecretKey = dotenv.get("SECRET_API_KEY")

  def generateToken(username: String, admin: Boolean): String =
    val now = Clock.systemUTC().instant().getEpochSecond
    val expiry = now + (5 * 3600000) / 1000

    var claim = JwtClaim(s"""{"user":$username, "admin":$admin}""")
    claim = claim.expiresAt(18000)

    Jwt.encode(claim, jwtSecretKey, JwtAlgorithm.HS256)


  def validateToken(token: String): Option[String] =
    Jwt.decodeRaw(token, jwtSecretKey, Seq(JwtAlgorithm.HS256)) match
      case Success(decoded) =>
        val payload = Json.parse(decoded)
        //val profileID = (payload \ "profile").as[Int]
        val username = (payload \ "user").as[String]
        Some(username)
      case Failure(_: JwtExpirationException) =>
        throw ExpiredToken()
      case Failure(_: JwtEmptyAlgorithmException) =>
        throw NotValidToken()
      case Failure(_) =>
        None

