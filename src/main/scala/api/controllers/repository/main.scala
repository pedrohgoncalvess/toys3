package pedro.goncalves
package api.controllers.repository


import akka.http.scaladsl.server.{Directives, Route}
import utils.configs.projectPath
import api.models
import s3.organizer.bucket.Bucket
import api.exceptions.repository.repositoryExceptionHandler
import s3.organizer.repository.Repository
import api.exceptions.bucket.BucketNotExists
import api.exceptions.repository.RepositoryExists
import scala.util.{Success, Failure}
import akka.http.scaladsl.model.StatusCodes


class main extends Directives with models.RepositoryJsonSupport:

  val rootDir: String = projectPath()

  val route: Route = pathPrefix("repository") {
    concat(
      post {
        handleExceptions(repositoryExceptionHandler) {
          entity(as[models.Repository]) { repository =>
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
      },
      delete {
        handleExceptions(repositoryExceptionHandler) {
          parameter("id".as[String]) { id =>
            //TODO: Implement, delete, edit and get routes 
            ???
          }
        }
      }
    )
  }
