package pedro.goncalves
package api.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class Repository(
                     name: String,
                     bucket_name:String,
                     versioned: Boolean,
                     )

trait RepositoryJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val repositoryFormat: RootJsonFormat[Repository] = jsonFormat3(Repository.apply)
  
}