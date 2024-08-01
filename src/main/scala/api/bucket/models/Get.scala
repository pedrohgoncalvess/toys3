package pedro.goncalves
package api.bucket.models


import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}



final case class Bucket(
                  id:String,
                  name:String
                          )

final case class Buckets(
                  buckets:Seq[Bucket]
                        )


trait GetJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val getFormat: RootJsonFormat[Bucket] = jsonFormat2(Bucket.apply)

  implicit val bucketsFormat: RootJsonFormat[Buckets] = jsonFormat1(Buckets.apply)

}

