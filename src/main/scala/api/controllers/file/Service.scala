package pedro.goncalves
package api.controllers.file

import api.controllers.file.Model
import s3.organizer.bucket.Bucket
import s3.organizer.repository.Repository
import utils.configs
import utils.configs.bucketsPath

import akka.http.scaladsl.server.directives.FileInfo

import java.io.File
import java.nio.file.{Files, Path}


object Service {

  def fileDestination(fileInfo: FileInfo)(implicit organizers: Model): File =
    val repositoryName = organizers.repository match
      case value => value
      case null => fileInfo.fileName.split("\\.").toList.head

    val versioned = organizers.versioned
    val version = organizers.version

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
  
  
  def completeStorage(
                       bucket:String, repository:Option[String],
                       versioned:Option[Boolean],
                       version:Option[Float]
                     ): Model =

    val _repository = repository match
      case Some(value) => value
      case _ => null
    
    val _versioned = versioned match
      case Some(value) => value
      case _ => false

  
    val _version = version match
      case Some(value) => value
      case _ => 1.0f
      
    Model(
      bucket=bucket,
      repository=_repository,
      versioned=_versioned,
      version=_version
    )
}
