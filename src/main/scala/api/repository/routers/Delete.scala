package pedro.goncalves
package api.repository.routers


import java.util.UUID
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.model.StatusCodes

import api.auth.Service.endpointAuthenticator
import api.repository.RepositoryJsonSupport
import api.repository.exceptions.DelTypeNotExists
import api.bucket.exceptions.BucketNotExists
import s3.organizer.implementations.{Bucket, Repository, listBuckets}


class Delete extends Directives with RepositoryJsonSupport:

  private val exc: ExecutionContext = global

  val route: Route = authenticateOAuth2(realm = "secure site", endpointAuthenticator) { auth =>
    authorize(true) { // TODO: Implement authorization logic    
      delete {
          parameters(
            "bucket-name".as[String],  //TODO: Change bucket name to bucket id
            "repository-id".as[String],
            "type".as[String]
          ) { (bucketName, repositoryID, delType) =>
            if (delType != "permanent" && delType != "soft")  //TODO: Make this verification equals of delete bucket
              throw DelTypeNotExists(delType)

            onComplete(listBuckets) {
              case Success(buckets) =>
                val validBuckets = buckets.filter(bucket => bucket.name == bucketName)
                if (validBuckets.isEmpty)
                  throw BucketNotExists(bucketName)
                val currentBucket = validBuckets.head

                onComplete(currentBucket.getRepositoryByID(UUID.fromString(repositoryID))) {
                  case Success(Some(repository)) =>

                    val (deleteOperation, typeOpr) =
                      if delType == "permanent" then (repository.exclude, "deleted")
                      else (repository._changeStatus(ex=exc, status=false, userID=UUID.fromString(auth)), "disabled")

                    onComplete(deleteOperation) {
                      case Success(_) => complete(StatusCodes.OK, s"Repository ${repository.name} has been $typeOpr.")
                      case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
                    }
                  case Failure(exception) => complete(StatusCodes.NotFound)
                }
            }
          }
        }
      }
    }
