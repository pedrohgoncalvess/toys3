package pedro.goncalves
package api.controllers.bucket.routers


import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import scala.util.{Failure, Success}
import api.controllers.bucket.BucketService.jsonBuckets
import api.controllers.bucket
import api.controllers.bucket.BucketJsonSupport


class Get extends Directives with BucketJsonSupport:

  val route: Route = get {
        onComplete(jsonBuckets) {
          case Success(buckets) =>
            complete(StatusCodes.OK, buckets)
          case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
      }
  }


