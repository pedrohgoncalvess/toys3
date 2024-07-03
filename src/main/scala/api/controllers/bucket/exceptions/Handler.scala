package pedro.goncalves
package api.controllers.bucket.exceptions


import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, extractUri}
import akka.http.scaladsl.server.ExceptionHandler
import api.controllers.repository.exceptions.DelTypeNotExists
import api.controllers.bucket.exceptions.{BucketExists, BucketNotExists}


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
    case e: DelTypeNotExists =>
      extractUri { _ =>
        complete(StatusCodes.NotFound, s"delete type doesn't ${e._type} exist. Options: ['permanent', 'soft']")
      }
