package pedro.goncalves
package api.route.file


import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.directives.FileInfo
import utils.configs
import java.io.File
import java.nio.file.{Files, Path}
import utils.configs.bucketsPath
import s3.organizer.bucket.Bucket
import s3.organizer.repository.Repository
import s3.structured.CSVFile
import scala.util.{Success, Failure}

class main extends Directives:
  
  def fileDestination(fileInfo: FileInfo)(implicit organizers:(String, String, Boolean, Float)): File =
    val repositoryName = organizers._2 match
      case value => value
      case null => fileInfo.fileName.split("\\.").toList.head

    val versioned = organizers._3
    val version = organizers._4
    
    val bucketName = organizers._1
    val bucketOperations = Bucket(bucketName)
    val repoOperations = Repository(bucketOperations, repositoryName)

    if (!repoOperations.check)
      repoOperations.create

    if (versioned)
      val lastVersion = repoOperations.lastVersion
      repoOperations.createVersion(lastVersion)
      val versionedPath = s"$bucketsPath\\$bucketName\\$repositoryName\\v${lastVersion+1f}\\${fileInfo.fileName}"
      Files.createFile(
        Path.of(versionedPath)
      ).toFile
    else
      Files.createFile(
          Path.of(s"$bucketsPath\\$bucketName\\$repositoryName\\${fileInfo.fileName}")
        ).toFile

  val route: Route = pathPrefix("file") {
    concat(
        post {
          parameter(
            "bucket".as[String], 
            "versioned".as[Boolean].optional,
            "repository".as[String].optional,
            "version".as[Float].optional
          ) { (bucket, rawVersioned, rawRepository, rawVersion) =>

            val versioned = rawVersioned match
              case Some(value) => value
              case _ => false

            val repositoryName = rawRepository match
              case Some(value) => value
              case _ => null

            val version = rawVersion match
              case Some(value) => value
              case _ => 1.0f

            //TODO: structure to organize the implicits that defines where the file will be saved

            val bucketOperations = Bucket(bucket)
            if (bucketOperations.check)

              val repository = Repository(bucketOperations, repositoryName)
              val lastVersion = repository.lastVersion

              if (lastVersion >= version)
                complete(StatusCodes.Conflict, s"Version must be greater than $lastVersion")

              else
                implicit val organizers: (String, String, Boolean, Float) = (bucket, repositoryName, versioned, version)

                storeUploadedFile("file", fileDestination) {
                  case (metadata, file) =>

                    val fileMetadata = CSVFile(repository, file.getPath, versioned, lastVersion)
                    onComplete(fileMetadata._generate) {
                      case Success(_) => complete(StatusCodes.OK)
                      case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
                    }
                }
                  
            else
              complete(StatusCodes.Conflict, s"Bucket $bucket not exists.")
          }
        }
    )
  }


