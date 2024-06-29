package pedro.goncalves
package api.controllers.repository

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class Repository(
                     name: String,
                     bucket_name:String,
                     versioned: Boolean,
                     )

case class Repositories(
                       repositories: Array[Repository]
                       )

trait RepositoryJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val repositoryFormat: RootJsonFormat[Repository] = jsonFormat3(Repository.apply)
  implicit val repositoriesFormat: RootJsonFormat[Repositories] = jsonFormat1(Repositories.apply)
  
}