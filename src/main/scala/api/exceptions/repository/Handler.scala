package pedro.goncalves
package api.exceptions.repository


import akka.http.scaladsl.server.Directives.{complete, extractUri}
import akka.http.scaladsl.server.ExceptionHandler
import api.exceptions.bucket.BucketNotExists
import akka.http.scaladsl.model.StatusCodes


implicit def repositoryExceptionHandler: ExceptionHandler =
  ExceptionHandler:
    case e: BucketNotExists =>
      extractUri { _ =>
        complete(StatusCodes.NotFound, s"Bucket ${e.bucketName} not exists.")
      }
    case e: RepositoryExists =>
      extractUri { _ =>
        complete(StatusCodes.Conflict, s"Repository ${e.repositoryName} already exists.")
      }
  
