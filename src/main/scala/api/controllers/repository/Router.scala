package pedro.goncalves
package api.controllers.repository


import akka.http.scaladsl.server.{Directives, Route}
import api.controllers.bucket.routers.{Get, Post, Delete}


class Router extends Directives:

  private val getMethod = new Get
  private val postMethod = new Post
  private val delMethod = new Delete

  val route: Route = pathPrefix("repository") { concat(getMethod.route, postMethod.route, delMethod.route) }
