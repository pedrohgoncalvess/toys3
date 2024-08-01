package pedro.goncalves


import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.util.{Failure, Success}
import java.nio.file.{Files, Paths}
import scala.io.StdIn
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import io.github.cdimascio.dotenv.Dotenv

import database.migration.Main.flyway
import database.operations.InteractUser.{createNewUser, existsAdmin}
import database.Connection.initializeDatabase


private def initializeService: Future[Unit] =

  val dotenv = Dotenv.load

  val s3Path = dotenv.get("DATA_PATH") match
    case null => "./toys3"
    case path => s"$path/toys3"

  if (!Files.exists(Paths.get(s3Path)))
    println(s"Created S3 data path in ${Paths.get(s3Path)}")
    Files.createDirectories(Paths.get(s3Path))
  else
    println(s"S3 data path already exists in ${Paths.get(s3Path)}")

  flyway.migrate()

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
  initializeDatabase

  println("Preparing env...")
  val preparingEnv = initializeService

  Await.result(preparingEnv, 5.seconds)

  val bucket = new api.bucket.Router
  val file = new api.file.Router
  val repository = new api.repository.Router
  val auth = new api.auth.Router

  val port = 8080

  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "my-system")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  val bindingFuture = Http().newServerAt("0.0.0.0", port).bind(Directives.concat(bucket.route, file.route, repository.route, auth.route))

  println(s"Server online at port $port.")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

