package pedro.goncalves
package api.route.bucket

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import utils.configs.projectPath
import scala.util.{Failure, Success}
import api.json
import api.json.Bucket
import api.services.buckets._

class BucketRoutes extends Directives with json.BucketJsonSupport {

  val rootDir: String = projectPath()

  val route: Route = pathPrefix("bucket") {
    concat(
        post {
          entity(as[json.Bucket]) { bucket =>
          val bucketExist = checkBucket(bucket.name)
          onComplete(bucketExist) {
            case Success(value) =>
             if (value) {
               complete(StatusCodes.Conflict, s"Bucket ${bucket.name} already exists.")
             } else {
               onComplete(createBucket(bucket.name)) {
                 case Success(_) => complete(StatusCodes.OK)
                 case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
               }
             }
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
          onComplete(checkBucket(bucket)) {
            case Success(value) =>
              if (value)
                if (delPermanent)
                  onComplete(deleteBucket(bucket)) {
                    case Success(_) => complete(StatusCodes.OK)
                    case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
                  }
                else 
                  onComplete(softDeleteBucket(bucket)) {
                    case Success(_) => complete(StatusCodes.OK)
                    case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
                  }
              else
                complete(StatusCodes.Conflict, s"Bucket $bucket not exists.")
          }
        }
      }
    )
  }
}


