package pedro.goncalves
package api.bucket.models


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}


final case class CreateBucket(
                       id:Option[String]=Some(null),
                       name: String,
                       tags: Option[Array[String]],
                       description: Option[String],
                     )


trait PostJsonSupport extends SprayJsonSupport with DefaultJsonProtocol:

  implicit val postFormat: RootJsonFormat[CreateBucket] = jsonFormat4(CreateBucket.apply)

