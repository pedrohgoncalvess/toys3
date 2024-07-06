package pedro.goncalves
package pedro.goncalves.api.auth

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}


final case class AuthCredentials(
                        username: String,
                        password: String
                       )


final case class AuthResponse(
                             token: String,
                             expire_at: String
                             )

trait AuthCredentialsJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val authCredentialsFormat: RootJsonFormat[AuthCredentials] = jsonFormat2(AuthCredentials.apply)
  implicit val authResponseFormat: RootJsonFormat[AuthResponse] = jsonFormat2(AuthResponse.apply)

}
