package pedro.goncalves
package api.route.bucket

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import utils.configs.projectPath
import scala.util.{Failure, Success}
import api.json
import api.services
import api.services.organizers.buckets.{Buckets, listBuckets}


class BucketRoutes extends Directives with json.BucketJsonSupport:

  val rootDir: String = projectPath()

  val route: Route = pathPrefix("bucket") {
    concat(
        post {
          entity(as[json.Bucket]) { bucket =>
          val bucketOperations = Buckets(bucket.name)
          
          val bucketExist = bucketOperations.check
          
            onComplete(bucketExist) {
            case Success(value) =>
             if (value) {
               complete(StatusCodes.Conflict, s"Bucket ${bucket.name} already exists.")
             } else {
               onComplete(bucketOperations.create) {
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
          val bucketOperations = Buckets(bucket)
          onComplete(bucketOperations.check) {
            case Success(value) =>
              if (value)
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
      }
    )
  }


