package pedro.goncalves
package api.auth


import akka.http.scaladsl.server.{Directives, Route}

import api.auth.exceptions.authExceptionHandler
import api.auth.routers.Post


class Router extends Directives:
  
  private val postMethod = new Post

  val route: Route = pathPrefix("auth") {
    handleExceptions(authExceptionHandler) {
      {
        concat(postMethod.route)
      }
    }
  }
