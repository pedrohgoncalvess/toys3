package pedro.goncalves
package api.repository


import akka.http.scaladsl.server.{Directives, Route}

import api.repository.exceptions.repositoryExceptionHandler
import api.repository.routers.{Get, Delete, Post}


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
