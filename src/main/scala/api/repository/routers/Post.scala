package pedro.goncalves
package pedro.goncalves.api.repository.routers

import akka.http.scaladsl.server.{Directives, Route}
import s3.organizer.bucket.Bucket
import api.controllers.repository.exceptions.repositoryExceptionHandler
import s3.organizer.repository.Repository

import scala.util.{Failure, Success}
import akka.http.scaladsl.model.StatusCodes
import api.controllers.auth.Service.endpointAuthenticator


class Post extends Directives with RepositoryJsonSupport:

  val route: Route = authenticateOAuth2(realm = "secure site", endpointAuthenticator) { auth =>
    authorize(true) { // TODO: Implement authorization logic    
      post {
          entity(as[repository.Repository]) { repository =>
            val bucketOrg = Bucket(repository.bucket_name)

            if (!bucketOrg.check)
              throw BucketNotExists(repository.bucket_name)

            val repositoryOrg = Repository(
              bucketOrg,
              repository.name,
              repository.versioned
            )

            if (repositoryOrg.check)
              throw RepositoryExists(repository.name)

            onComplete(repositoryOrg.create) {
              case Success(_) => complete(StatusCodes.Created, "Created")
              case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
            }
          }
        }
      }
    }
