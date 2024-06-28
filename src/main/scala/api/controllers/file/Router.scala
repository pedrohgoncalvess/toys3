package pedro.goncalves
package api.controllers.file


import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import api.controllers.bucket.routers.Post


class Router extends Directives:
  
  private val postMethod = new Post

  val route: Route = pathPrefix("file") { concat(postMethod.route) }


