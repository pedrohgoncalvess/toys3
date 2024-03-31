package pedro.goncalves

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn
import api.controllers


@main def main() =
  import api.controllers.bucket.main
  import api.controllers.file.main

  val bucket = new controllers.bucket.main
  val file = new controllers.file.main
  val repository = new controllers.repository.main
  
  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "my-system")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  val bindingFuture = Http().newServerAt("0.0.0.0", 8080).bind(Directives.concat(bucket.route, file.route, repository.route))

  println(s"Server online at port 8080.")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

