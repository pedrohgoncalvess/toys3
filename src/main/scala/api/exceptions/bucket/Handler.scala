package pedro.goncalves
package api.exceptions.bucket


import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, extractUri}
import akka.http.scaladsl.server.ExceptionHandler


implicit def bucketExceptionHandler: ExceptionHandler =
  ExceptionHandler:
    case e: BucketNotExists =>
      extractUri { _ =>
        complete(StatusCodes.NotFound, s"Bucket ${e.name} not exists.")
      }
    case e: BucketExists =>
      extractUri { _ =>
        complete(StatusCodes.Conflict, s"Bucket ${e.name} already exists.")
      }
    case e:UUIdBucketNotExists =>
      extractUri { _ =>
        complete(StatusCodes.NotFound, s"UUID ${e.id} is not linked to any bucket.")
        
      }
