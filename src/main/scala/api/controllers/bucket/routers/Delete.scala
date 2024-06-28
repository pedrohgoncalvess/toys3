package pedro.goncalves
package api.controllers.bucket.routers


import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import utils.configs.projectPath
import api.exceptions.bucket.bucketExceptionHandler
import scala.util.{Failure, Success}
import s3.organizer.bucket.Bucket
import api.exceptions.repository.DelTypeNotExists
import api.controllers.bucket
import api.controllers.bucket.exceptions.BucketNotExists
import api.controllers.bucket.BucketJsonSupport
import api.controllers.bucket.BucketService.jsonBuckets


class Delete extends Directives with BucketJsonSupport:

  val rootDir: String = projectPath()

  import scala.concurrent.ExecutionContext.Implicits.global

  val route: Route = delete {
        handleExceptions(bucketExceptionHandler) {
          parameter("name".as[String], "type".as[String]) { (bucketName, delType) =>

            val deleteTypes = Array("permanent", "soft")

            if (deleteTypes.contains(delType))
              throw DelTypeNotExists(delType)

            onComplete(jsonBuckets) {
              case Success(buckets) =>

                val bucket = buckets.buckets.filter(bucket => bucket.name == bucketName)

                if (bucket.isEmpty)
                  throw BucketNotExists(bucketName)

                val bucketOperations = Bucket(bucket.head.name)

                val (delOperation, typeOpr) = if delType=="permanent" then (bucketOperations.exclude, "deleted") else (bucketOperations._disability, "disabled")

                onComplete(delOperation) {
                  case Success(_) => complete(StatusCodes.OK, s"Bucket $bucketName has been $typeOpr.")
                  case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
                }

              case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
          }
        }
      }
    }


