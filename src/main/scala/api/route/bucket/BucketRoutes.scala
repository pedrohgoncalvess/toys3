package pedro.goncalves
package api.route.bucket

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import utils.configs.projectPath
import java.io.File
import scala.concurrent.Future
import scala.util.{Failure, Success}
import api.json
import api.json.Bucket


class BucketRoutes extends Directives with json.BucketJsonSupport {

  import scala.concurrent.ExecutionContext.Implicits.global

  val rootDir: String = projectPath()

  val route: Route = pathPrefix("bucket") {
    concat(
        post {
          entity(as[json.Bucket]) { bucket =>
          val dir = File(s"$rootDir\\toys3\\buckets\\${bucket.name}")
          if (dir.exists()) {
            complete(StatusCodes.Conflict, s"Bucket ${bucket.name} already exists.")
          } else {
            val createDir:Future[Unit] = Future {
              dir.mkdir()
            }
            onComplete(createDir){
              case Success(_) =>
                complete(StatusCodes.OK, s"Bucket ${bucket.name} created.")
              case Failure(exception) =>
                complete(StatusCodes.InternalServerError, exception.getMessage)
            }
          }
        }
      },
      get {
        val rawBuckets = File(s"$rootDir\\toys3\\buckets").listFiles()
        val buckets = rawBuckets.map(bucket => Bucket(name=bucket.getName))
        complete(StatusCodes.OK, buckets)
      }
    )
  }
}


