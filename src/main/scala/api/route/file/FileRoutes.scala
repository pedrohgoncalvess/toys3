package pedro.goncalves
package api.route.file

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.directives.FileInfo
import utils.configs.projectPath
import java.io.File
import java.nio.file.{Files, Path}

class FileRoutes extends Directives {

  def fileDestination(fileInfo: FileInfo)(implicit bucket:String): File =
    if (bucket == "")
      Files.createFile(Path.of(s"$rootDir\\toys3\\${fileInfo.fileName}")).toFile
    else
      Files.createFile(Path.of(s"$rootDir\\toys3\\$bucket\\${fileInfo.fileName}")).toFile

  val rootDir = projectPath()

  val route: Route = pathPrefix("file") {
    concat(
      path("upload") {
        post {
          parameter("bucket") { bucket =>
            implicit val bucketToSave: String = bucket
            storeUploadedFile("file", fileDestination) {
              case (metadata, file) =>
                val filePath = file.getAbsolutePath
                complete(StatusCodes.OK, s"Arquivo importado. Caminho $filePath")
            }
          }
        }
      }
    )
  }
}


