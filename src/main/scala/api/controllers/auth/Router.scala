package pedro.goncalves
package api.controllers.auth


import akka.http.scaladsl.server.{Directives, Route}
import api.controllers.auth.routers.Post
import api.controllers.auth.exceptions.authExceptionHandler


class Router extends Directives:
  
  private val postMethod = new Post

  val route: Route = 
    pathPrefix("auth") {
    handleExceptions(authExceptionHandler) {
      {
        concat(postMethod.route)
      }
    }
  }
