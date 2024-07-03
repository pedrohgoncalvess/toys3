package pedro.goncalves
package api.controllers.repository


import akka.http.scaladsl.server.{Directives, Route}
import api.controllers.repository.routers.{Get, Post, Delete}
import api.controllers.repository.exceptions.repositoryExceptionHandler


class Router extends Directives:

  private val getMethod = new Get
  private val postMethod = new Post
  private val delMethod = new Delete

  val route: Route = pathPrefix("repository") {
    handleExceptions(repositoryExceptionHandler) {
      {
        concat(getMethod.route, postMethod.route, delMethod.route)
      }
    }
  }
