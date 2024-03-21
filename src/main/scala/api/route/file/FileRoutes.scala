package pedro.goncalves
package api.route.file

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.directives.FileInfo
import api.services.buckets.checkBucket
import utils.configs

import scala.util.Success
import java.io.File
import java.nio.file.{Files, Path}
import api.services.file.{createRepository, deleteRepository}

class FileRoutes extends Directives {


  def fileDestination(fileInfo: FileInfo)(implicit bucket:String): File =
    val repoName = fileInfo.fileName.split("\\.").toList.head
    createRepository(bucket, repoName)
    Files.createFile(Path.of(s"$bucketsPath\\$bucket\\$repoName\\${fileInfo.fileName}")).toFile


  val bucketsPath = configs.bucketsPath

  val route: Route = pathPrefix("file") {
    concat(
        post {
          parameter("bucket".as[String], "separator".as[String].optional) { (bucket, separator) =>
            implicit val bucketToSave: String = bucket
            onComplete(checkBucket(bucket)) {
              case Success(value) =>
                if (value)
                  storeUploadedFile("file", fileDestination) {
                    case (metadata, file) =>
                      if (metadata.fileName.matches(".*\\.csv") && separator.orNull == null)
                        deleteRepository(bucket, metadata.fileName.split("\\.").toList.head)
                        complete(StatusCodes.UnprocessableContent, s"${metadata.fileName} needs a separator.")
                      else
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


