package pedro.goncalves
package api.services


import akka.http.scaladsl.server.directives.FileInfo
import utils.configs
import java.io.File
import java.nio.file.{Files, Path}
import utils.configs.bucketsPath
import s3.organizer.bucket.Bucket
import s3.organizer.repository.Repository
import api.models.FileStorage


object File {

  def fileDestination(fileInfo: FileInfo)(implicit organizers: FileStorage): File =
    val repositoryName = organizers._2 match
      case value => value
      case null => fileInfo.fileName.split("\\.").toList.head

    val versioned = organizers._3
    val version = organizers._4

    val bucketName = organizers._1
    val bucketOperations = Bucket(bucketName)
    val repoOperations = Repository(bucketOperations, repositoryName)

    if (versioned)
      val lastVersion = repoOperations.lastVersion
      repoOperations.createVersion(lastVersion)
      val versionedPath = s"$bucketsPath\\$bucketName\\$repositoryName\\v${lastVersion + 1f}\\${fileInfo.fileName}"
      Files.createFile(
        Path.of(versionedPath)
      ).toFile

    else
      Files.createFile(
        Path.of(s"$bucketsPath\\$bucketName\\$repositoryName\\${fileInfo.fileName}")
      ).toFile
  
  
  def completeStorage(bucket:String, repository:Option[String], versioned:Option[Boolean], version:Option[Float]): FileStorage =

    val _repository = repository match
      case Some(value) => value
      case _ => null
    
    val _versioned = versioned match
      case Some(value) => value
      case _ => false

  
    val _version = version match
      case Some(value) => value
      case _ => 1.0f
      
    FileStorage(
      bucket=bucket,
      repository=_repository,
      versioned=_versioned,
      version=_version
    )
}
