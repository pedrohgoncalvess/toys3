package pedro.goncalves
package api.bucket.routers


import scala.util.{Failure, Success}

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes


import s3.organizer.bucket.Bucket
import api.bucket.BucketJsonSupport
import api.auth.Service.endpointAuthenticator


class Post extends Directives with BucketJsonSupport:

  val route: Route = authenticateOAuth2(realm = "secure site", endpointAuthenticator) { auth =>
    authorize(true) { // TODO: Implement authorization logic 
      post {
        entity(as[api.bucket.Bucket]) { bucket =>
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
    }
  }


