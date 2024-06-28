package pedro.goncalves
package api.models

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

final case class Bucket(
                         name: String
                       )

final case class Buckets(
                  buckets:Seq[Bucket]
                  )


trait BucketJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val bucketFormat: RootJsonFormat[Bucket] = jsonFormat1(Bucket.apply)

  implicit val bucketsFormat: RootJsonFormat[Buckets] = jsonFormat1(Buckets.apply)

}

