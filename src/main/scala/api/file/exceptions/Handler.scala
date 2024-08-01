package pedro.goncalves
package api.file.exceptions


import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, extractUri}
import akka.http.scaladsl.server.ExceptionHandler

import api.bucket.exceptions.BucketNotExists
import api.auth.exceptions.authExceptionHandler


implicit def fileExceptionHandler: ExceptionHandler =
  ExceptionHandler {
    case _: InconsistentParameters =>
      extractUri { _ =>
        complete(StatusCodes.UnprocessableContent, "If versioned needs a repository name.")
      }

    case e: InconsistentRepositoryVersion =>
      extractUri { _ =>
        complete(StatusCodes.Conflict, s"Version must be greater than ${e.version}.")
      }

    case e: BucketNotExists =>
      extractUri { _ =>
        complete(StatusCodes.NotFound, s"Bucket ${e.name} not exists.")
      }
  }.withFallback(authExceptionHandler)