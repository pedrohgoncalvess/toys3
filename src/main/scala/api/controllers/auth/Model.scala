package pedro.goncalves
package api.controllers.auth


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}


final case class AuthCredentials(
                        username: String,
                        password: String
                       )


trait AuthCredentialsJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val authCredentialsFormat: RootJsonFormat[AuthCredentials] = jsonFormat2(AuthCredentials.apply)

}
