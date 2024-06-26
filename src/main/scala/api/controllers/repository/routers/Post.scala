package pedro.goncalves
package api.controllers.repository.routers


import akka.http.scaladsl.server.{Directives, Route}
import utils.configs.projectPath
import s3.organizer.bucket.{Bucket, listBuckets}
import api.exceptions.repository.{DelTypeNotExists, RepositoryExists, RepositoryNotExists, repositoryExceptionHandler}
import s3.organizer.repository.Repository
import scala.util.{Failure, Success}
import akka.http.scaladsl.model.StatusCodes
import api.controllers.bucket.BucketService.jsonBuckets
import api.controllers.repository.Service.jsonRepositories
import api.controllers.bucket.exceptions.BucketNotExists
import api.controllers.repository.{RepositoryJsonSupport, Repository as RepositoryJson}


class Post extends Directives with RepositoryJsonSupport:

  val route: Route = post {
        handleExceptions(repositoryExceptionHandler) {
          entity(as[RepositoryJson]) { repository =>
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
