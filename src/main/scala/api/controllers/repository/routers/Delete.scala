package pedro.goncalves
package api.controllers.repository.routers


import akka.http.scaladsl.server.{Directives, Route}
import utils.configs.projectPath
import s3.organizer.bucket.{Bucket, listBuckets}
import api.exceptions.repository.{DelTypeNotExists, RepositoryNotExists, repositoryExceptionHandler}
import s3.organizer.repository.Repository
import scala.util.{Failure, Success}
import akka.http.scaladsl.model.StatusCodes
import api.controllers.bucket.exceptions.BucketNotExists
import api.controllers.repository.RepositoryJsonSupport


class Delete extends Directives with RepositoryJsonSupport:

  import scala.concurrent.ExecutionContext.Implicits.global

  val rootDir: String = projectPath()

  val route: Route = delete {
        handleExceptions(repositoryExceptionHandler) {
          parameters(
            "bucket-name".as[String],
            "repository-name".as[String],
            "type".as[String]
          ) { (bucketName, repositoryName, delType) =>
            if (delType != "permanent" && delType != "soft")
              throw DelTypeNotExists(delType)

            onComplete(listBuckets) {
              case Success(buckets) =>
                val validBuckets = buckets.filter(bucket => bucket.name == bucketName)
                if (validBuckets.isEmpty)
                  throw BucketNotExists(bucketName)
                val currentBucket = validBuckets.head

                onComplete(currentBucket.listRepositories) {
                  case Success(repositories) =>
                    val validRepositories = repositories.filter(_.name == repositoryName)

                    if (validRepositories.isEmpty)
                      throw RepositoryNotExists(repositoryName)

                    val currentRepository = validRepositories.head

                    val (deleteOperation, typeOpr) = if delType == "permanent" then (currentRepository.exclude, "deleted") else (currentRepository._disability, "disabled")

                    onComplete(deleteOperation) {
                      case Success(_) => complete(StatusCodes.OK, s"Repository $repositoryName has been $typeOpr.")
                      case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
                }
              }
            }
          }
        }
      }
