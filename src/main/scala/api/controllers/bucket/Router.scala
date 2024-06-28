package pedro.goncalves
package api.controllers.bucket


import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import api.controllers.bucket.routers.{Get, Post, Delete}


class Router extends Directives:

  private val getMethod = new Get
  private val delMethod = new Delete
  private val postMethod = new Post

  val route: Route = pathPrefix("bucket") { concat( getMethod.route, delMethod.route, postMethod.route) }


