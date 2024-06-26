package pedro.goncalves
package api.controllers.bucket.routers


import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import scala.util.{Failure, Success}
import s3.organizer.bucket.Bucket
import api.controllers.bucket
import api.controllers.bucket.BucketJsonSupport


class Post extends Directives with BucketJsonSupport:

  val route: Route = post {
        entity(as[bucket.Bucket]) { bucket =>
          val bucketOperations = Bucket(bucket.name)

          if (bucketOperations.check)
            complete(StatusCodes.Conflict, s"Bucket ${bucket.name} already exists.")

          else
            onComplete(bucketOperations.create) {
              case Success(_) => complete(StatusCodes.OK)
              case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
          }
      }
  }


