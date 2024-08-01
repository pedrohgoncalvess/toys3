package pedro.goncalves
package api.bucket.routers


import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes

import api.bucket.BucketService.jsonBuckets
import api.auth.Service.endpointAuthenticator
import api.repository.exceptions.DelTypeNotExists
import api.bucket.exceptions.BucketNotExists
import s3.organizer.implementations.Bucket


class Delete extends Directives:
  
  import scala.concurrent.ExecutionContext.Implicits.global
  
  private val exc: ExecutionContext = global

  val route: Route = authenticateOAuth2(realm = "secure site", endpointAuthenticator) { auth =>
    authorize(true) { // TODO: Implement authorization logic
      delete {
        parameter("id".as[String], "type".as[String]) { (bucketID, delType) =>

          val deleteTypes = Array("permanent", "soft")

          if (!deleteTypes.contains(delType))
            throw DelTypeNotExists(delType)

          onComplete(jsonBuckets) {
            case Success(buckets) =>

              val bucket = buckets.buckets.filter(bucket => bucket.id == bucketID)

              if (bucket.isEmpty)
                throw BucketNotExists(bucketID)

              val bucketOperations = Bucket(bucket.head.name)

              val (delOperation, typeOpr) = 
                if delType == "permanent" then (bucketOperations.exclude, "deleted")
                else (bucketOperations._changeStatus(ex=exc, status=false, userID=UUID.fromString(auth)), "disabled")

              onComplete(delOperation) {
                case Success(_) => complete(StatusCodes.OK, s"Bucket has been $typeOpr.")
                case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
              }

            case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
          }
        }
      }
    }
  }


