package pedro.goncalves
package api.repository.routers


import scala.util.{Failure, Success}

import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.model.StatusCodes

import api.repository.exceptions.repositoryExceptionHandler
import api.repository.RepositoryJsonSupport
import api.repository.Repository as RepositoryBody
import api.auth.Service.endpointAuthenticator
import api.bucket.exceptions.BucketNotExists
import api.repository.exceptions.RepositoryExists
import s3.organizer.repository.Repository
import s3.organizer.bucket.Bucket


class Post extends Directives with RepositoryJsonSupport:

  val route: Route = authenticateOAuth2(realm = "secure site", endpointAuthenticator) { auth =>
    authorize(true) { // TODO: Implement authorization logic    
      post {
          entity(as[RepositoryBody]) { repository =>
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
