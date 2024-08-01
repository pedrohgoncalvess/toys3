package pedro.goncalves
package api.repository.exceptions


import akka.http.scaladsl.server.Directives.{complete, extractUri}
import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.model.StatusCodes

import api.auth.exceptions.{ExpiredToken, NotValidToken}
import api.bucket.exceptions.BucketNotExists
import api.auth.exceptions.authExceptionHandler


implicit def repositoryExceptionHandler: ExceptionHandler =
  ExceptionHandler {

    case e: BucketNotExists =>
      extractUri { _ =>
        complete(StatusCodes.NotFound, s"Bucket ${e.name} not exists.")
      }

    case e: RepositoryExists =>
      extractUri { _ =>
        complete(StatusCodes.Conflict, s"Repository ${e.name} already exists.")
      }

    case e: DelTypeNotExists =>
      extractUri { _ =>
        complete(StatusCodes.NotFound, s"delete type doesn't ${e._type} exist. Options: ['permanent', 'soft']")
      }

    case e: NotValidToken =>
      extractUri { _ =>
        complete(StatusCodes.Unauthorized, e.getMessage)
      }

    case e: ExpiredToken =>
      extractUri { _ =>
        complete(StatusCodes.Unauthorized, e.getMessage)
      }
  }.withFallback(authExceptionHandler)