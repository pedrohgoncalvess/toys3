package pedro.goncalves


import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.io.StdIn
import api.controllers
import database.migration.Migration.flyway
import io.github.cdimascio.dotenv.Dotenv
import database.operations.InteractUser.{createNewUser, existsAdmin}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

private def initializeService: Future[Unit] =

  flyway.migrate()

  val dotenv = Dotenv.load

  val envUsername = dotenv.get("TOY_S3_ROOT_USER")
  val envPassword = dotenv.get("TOY_S3_ROOT_PASSWORD")

  val adminUsername = envUsername match
    case user => user
    case null => "admin"

  val adminPassword = envPassword match
    case null => "admin"
    case pass => pass


  existsAdmin.map( t =>
    if (!t) createNewUser(user = adminUsername, password = adminPassword, admin = true, id_profile = Some(null))
  )


@main def Main(): Unit =
  import api.controllers.bucket.Router
  import api.controllers.file.Router

  val preparingEnv = initializeService
  println("Preparing env...")

  Await.result(preparingEnv, 5.seconds)

  val bucket = new controllers.bucket.Router
  val file = new controllers.file.Router
  val repository = new controllers.repository.Router

  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "my-system")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  val bindingFuture = Http().newServerAt("0.0.0.0", 8080).bind(Directives.concat(bucket.route, file.route, repository.route))

  println(s"Server online at port 8080.")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

