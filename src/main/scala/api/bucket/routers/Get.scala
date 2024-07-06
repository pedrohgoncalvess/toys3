package pedro.goncalves
package api.bucket.routers


import scala.util.{Failure, Success}

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes

import api.bucket.BucketService.jsonBuckets
import api.bucket.BucketJsonSupport
import api.auth.Service.endpointAuthenticator


class Get extends Directives with BucketJsonSupport:

  val route: Route = authenticateOAuth2(realm = "secure site", endpointAuthenticator) { auth =>
    authorize(true) { // TODO: Implement authorization logic
      get {
        onComplete(jsonBuckets) {
          case Success(buckets) =>
            complete(StatusCodes.OK, buckets)
          case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }


