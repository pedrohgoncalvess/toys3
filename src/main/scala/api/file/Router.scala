package pedro.goncalves
package pedro.goncalves.api.file

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import api.controllers.file.exceptions.fileExceptionHandler


class Router extends Directives:
  
  private val postMethod = new Post

  val route: Route = pathPrefix("file") {
    handleExceptions(fileExceptionHandler) {
      {
        concat(postMethod.route)
      }
    }
  }


