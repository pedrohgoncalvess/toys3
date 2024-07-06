package pedro.goncalves
package api.auth.exceptions


import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, extractUri}
import akka.http.scaladsl.server.{Directives, ExceptionHandler}


implicit def authExceptionHandler: ExceptionHandler =
  ExceptionHandler {

    case e: NotValidToken =>
      extractUri { _ =>
        complete(StatusCodes.Unauthorized, e.getMessage)
      }

    case e: ExpiredToken =>
      extractUri { _ =>
        complete(StatusCodes.Unauthorized, e.getMessage)
      }
  }