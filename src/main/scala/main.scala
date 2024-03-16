package pedro.goncalves

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn


@main def main() =
  import api.route.bucket.BucketRoutes
  import api.route.file.FileRoutes

  val bucket = new BucketRoutes
  val file = new FileRoutes
  
  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "my-system")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  val bindingFuture = Http().newServerAt("0.0.0.0", 8080).bind(Directives.concat(bucket.route, file.route))

  println(s"Server now online at port 8080.")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

