package pedro.goncalves
package api.controllers.repository


import akka.http.scaladsl.server.{Directives, Route}
import utils.configs.projectPath
import api.models
import s3.organizer.bucket.{Bucket, listBuckets}
import api.exceptions.repository.{DelTypeNotExists, RepositoryExists, RepositoryNotExists, repositoryExceptionHandler}
import s3.organizer.repository.Repository

import scala.util.{Failure, Success}
import akka.http.scaladsl.model.StatusCodes
import api.services.Bucket.jsonBuckets
import api.services.Repository.jsonRepositories
import pedro.goncalves.api.controllers.bucket.exceptions.BucketNotExists


class main extends Directives with models.RepositoryJsonSupport:
  
  import scala.concurrent.ExecutionContext.Implicits.global

  val rootDir: String = projectPath()

  val route: Route = pathPrefix("repository") {
    concat(
      get {
        handleExceptions(repositoryExceptionHandler) {
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
      },
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
          parameters(
            "bucket-name".as[String],
            "repository-name".as[String],
            "type".as[String]
          ) { (bucketName, repositoryName, delType) =>
            if (delType != "permanent" && delType != "soft")
              throw DelTypeNotExists(delType)

            onComplete(listBuckets) {
              case Success(buckets) =>
                val validBuckets = buckets.filter(bucket => bucket.name==bucketName)
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
    )
  }
