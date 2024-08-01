package pedro.goncalves
package api.repository.routers


import scala.util.{Failure, Success}

import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.model.StatusCodes

import api.bucket.BucketService.jsonBuckets
import api.repository.Service.jsonRepositories
import api.auth.Service.endpointAuthenticator
import api.repository.RepositoryJsonSupport
import api.bucket.exceptions.BucketNotExists


class Get extends Directives with RepositoryJsonSupport:

  val route: Route = authenticateOAuth2(realm = "secure site", endpointAuthenticator) { auth =>
    authorize(true) { // TODO: Implement authorization logic    
      get {
          parameter("bucket-id".as[String]) { idBucket =>
            onComplete(jsonBuckets) {
              case Success(buckets) =>
                val currentBucket = buckets.buckets.filter(_.id == idBucket)
                if (currentBucket.isEmpty)
                  throw BucketNotExists(idBucket)

                val repositories = jsonRepositories(Some(currentBucket.head))

                onComplete(repositories) {
                  case Success(repos) => complete(StatusCodes.OK, repos)
                  case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
                }
              case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
            }
          }
        }
      }
    }
