package pedro.goncalves
package pedro.goncalves.api.repository.routers

import akka.http.scaladsl.server.{Directives, Route}
import utils.configs.projectPath

import scala.util.{Failure, Success}
import akka.http.scaladsl.model.StatusCodes
import api.controllers.bucket.BucketService.jsonBuckets
import api.controllers.repository.Service.jsonRepositories
import api.controllers.auth.Service.endpointAuthenticator


class Get extends Directives with RepositoryJsonSupport:
  
  val rootDir: String = projectPath()

  val route: Route = authenticateOAuth2(realm = "secure site", endpointAuthenticator) { auth =>
    authorize(true) { // TODO: Implement authorization logic    
      get {
          parameter("bucket-name".as[String]) { nameBucket =>
            onComplete(jsonBuckets) {
              case Success(buckets) =>
                val currentBucket = buckets.buckets.filter(_.name == nameBucket)
                if (currentBucket.isEmpty)
                  throw BucketNotExists(nameBucket)

                val repositories = jsonRepositories(currentBucket.head)

                onComplete(repositories) {
                  case Success(repos) => complete(StatusCodes.OK, repos)
                  case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
                }
            }
          }
        }
      }
    }
