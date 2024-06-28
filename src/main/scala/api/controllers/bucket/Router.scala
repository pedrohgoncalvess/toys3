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
import api.services.Bucket.jsonBuckets
import api.exceptions.repository.DelTypeNotExists

import pedro.goncalves.api.controllers.bucket.exceptions.BucketNotExists


class Main extends Directives with models.BucketJsonSupport:

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
        onComplete(jsonBuckets) {
          case Success(buckets) =>
            complete(StatusCodes.OK, buckets)
          case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      },
      
      delete {
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
    )
  }


