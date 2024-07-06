package pedro.goncalves
package api.file


import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route

import api.file.exceptions.fileExceptionHandler
import api.file.routers.Post


class Router extends Directives:
  
  private val postMethod = new Post

  val route: Route = pathPrefix("file") {
    handleExceptions(fileExceptionHandler) {
      {
        concat(postMethod.route)
      }
    }
  }


