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
import java.nio.file.{Files, Paths}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.util.{Failure, Success}


private def initializeService: Future[Unit] =

  flyway.migrate()

  val dotenv = Dotenv.load

  val s3Path = dotenv.get("DATA_PATH") match
    case null => "./toys3"
    case path => s"$path/toys3"

  if (!Files.exists(Paths.get(s3Path)))
      println(s"Created S3 data path in ${Paths.get(s3Path)}")
      Files.createDirectories(Paths.get(s3Path))
  else
    println(s"S3 data path already exists in ${Paths.get(s3Path)}")

  existsAdmin.map( t =>
    if (!t) {

      val envUsername = dotenv.get("TOY_S3_ROOT_USER") match
        case null => "admin"
        case user => user

      val envPassword = dotenv.get("TOY_S3_ROOT_PASSWORD") match
        case null => "admin"
        case pass => pass

      if (envUsername == "admin" || envPassword == "admin")
        println("The system is using default credentials. Consider changing for added security.")

      val creatingNewUser = createNewUser(user = envUsername, password = envPassword, admin = true, id_profile = Some(null))
      creatingNewUser.onComplete{
        case Success(_) => Future.successful
        case Failure(exception) => throw exception
      }
    }
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
  val auth = new controllers.auth.Router

  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "my-system")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  val bindingFuture = Http().newServerAt("0.0.0.0", 8080).bind(Directives.concat(bucket.route, file.route, repository.route, auth.route))

  println(s"Server online at port 8080.")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

