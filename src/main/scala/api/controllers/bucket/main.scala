package pedro.goncalves
package api.controllers.bucket


import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import utils.configs.projectPath
import api.exceptions.bucket.bucketExceptionHandler
import scala.util.{Failure, Success}
import api.models
import s3.organizer.bucket.Bucket
import api.services.Bucket.idNameBucket
import api.exceptions.bucket.{BucketNotExists, UUIdBucketNotExists}


class main extends Directives with models.BucketJsonSupport:

  val rootDir: String = projectPath()

  import scala.concurrent.ExecutionContext.Implicits.global

  val route: Route = pathPrefix("bucket") {
    concat(
        post {
          entity(as[models.Bucket]) { bucket =>
          val bucketOperations = Bucket(bucket.name)

          if (bucketOperations.check)
           complete(StatusCodes.Conflict, s"Bucket ${bucket.name} already exists.")
           
          else
           onComplete(bucketOperations.create) {
             case Success(_) => complete(StatusCodes.OK)
             case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
           }
        }
      },
      
      get {
        onComplete(idNameBucket) {
          case Success(buckets) =>
            complete(StatusCodes.OK, buckets)
          case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      },
      
      delete {
        handleExceptions(bucketExceptionHandler) {
          parameter("id".as[String], "permanent".as[Boolean]) { (bucketId, delPermanent) =>
            onComplete(idNameBucket) {
              case Success(buckets) =>

                val bucket = buckets.buckets.filter(bucket => bucket.id.get == bucketId)

                if (bucket.isEmpty)
                  throw UUIdBucketNotExists(bucketId)

                val bucketOperations = Bucket(bucket.head.name)
                if (delPermanent)
                  onComplete(bucketOperations.exclude) {
                    case Success(_) => complete(StatusCodes.OK)
                    case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
                  }
                else
                  onComplete(bucketOperations._disability) {
                    case Success(_) => complete(StatusCodes.OK)
                    case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
              }
            }
          }
        }
      }
    )
  }


