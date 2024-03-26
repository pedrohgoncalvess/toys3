package pedro.goncalves
package api.route.file

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.directives.FileInfo
import api.services
import utils.configs

import scala.util.Success
import java.io.File
import java.nio.file.{Files, Path}
import api.services.objects

import pedro.goncalves.api.services.organizers.buckets.Buckets
import pedro.goncalves.api.services.organizers.repositorys.Repository

class FileRoutes extends Directives {
  
  def fileDestination(fileInfo: FileInfo)(implicit bucket:String): File =
    val repoName = fileInfo.fileName.split("\\.").toList.head
    
    val bucketOperations = new Buckets(bucket)
    val repoOperations = new Repository(bucketOperations, repoName)
    repoOperations.create
    Files.createFile(Path.of(s"$bucketsPath\\$bucket\\$repoName\\${fileInfo.fileName}")).toFile


  val bucketsPath = configs.bucketsPath

  val route: Route = pathPrefix("file") {
    concat(
        post {
          parameter("bucket".as[String], "separator".as[String].optional) { (bucket, separator) =>
            implicit val bucketToSave: String = bucket
            val bucketOperations = new Buckets(bucket)
            onComplete(bucketOperations.check) {
              case Success(value) =>
                
                if (value)
                  storeUploadedFile("file", fileDestination) {
                    case (metadata, file) =>
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


