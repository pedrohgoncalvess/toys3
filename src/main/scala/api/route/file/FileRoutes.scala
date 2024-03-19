package pedro.goncalves
package api.route.file

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.directives.FileInfo
import api.services.buckets.checkBucket
import utils.configs
import scala.util.{Failure, Success}
import java.io.File
import java.nio.file.{Files, Path}

class FileRoutes extends Directives {

  def fileDestination(fileInfo: FileInfo)(implicit bucket:String): File =
    Files.createFile(Path.of(s"$bucketsPath\\$bucket\\${fileInfo.fileName}")).toFile

  val bucketsPath = configs.bucketsPath

  val route: Route = pathPrefix("file") {
    concat(
        post {
          parameter("bucket") { bucket =>
            implicit val bucketToSave: String = bucket
            onComplete(checkBucket(bucket)) {
              case Success(value) =>
                if (value)
                  storeUploadedFile("file", fileDestination) {
                    case (metadata, file) =>
                      val filePath = file.getAbsolutePath
                      complete(StatusCodes.OK)
                  }
                else
                  complete(StatusCodes.Conflict, s"Bucket $bucket not exists.")
            }
          }
        }
    )
  }
}


