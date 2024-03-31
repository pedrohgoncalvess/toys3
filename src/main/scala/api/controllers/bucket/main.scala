package pedro.goncalves
package api.controllers.bucket


import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import utils.configs.projectPath
import scala.util.{Failure, Success}
import api.models
import s3.organizer.bucket.{listBuckets, Bucket}


class main extends Directives with models.BucketJsonSupport:

  val rootDir: String = projectPath()

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
        onComplete(listBuckets) {
          case Success(buckets) =>
            complete(StatusCodes.OK, buckets)
          case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      },
      
      delete {
        parameter("name".as[String], "permanent".as[Boolean]) { (bucket, delPermanent) =>
          val bucketOperations = Bucket(bucket)
          if (bucketOperations.check)
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
          else
            complete(StatusCodes.Conflict, s"Bucket $bucket not exists.")
          }
        }
    )
  }


