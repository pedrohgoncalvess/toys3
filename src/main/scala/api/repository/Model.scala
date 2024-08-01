package pedro.goncalves
package api.repository

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

case class Repository(
                     id:Option[String],
                     name: String,
                     bucket_name:String,
                     versioned: Boolean,
                     )

case class Repositories(
                       repositories: Array[Repository]
                       )

trait RepositoryJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val repositoryFormat: RootJsonFormat[Repository] = jsonFormat4(Repository.apply)
  implicit val repositoriesFormat: RootJsonFormat[Repositories] = jsonFormat1(Repositories.apply)
  
}