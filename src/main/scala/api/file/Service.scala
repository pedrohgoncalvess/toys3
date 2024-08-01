package pedro.goncalves
package api.file


import java.io.File
import java.nio.file.{Files, Path}

import akka.http.scaladsl.server.directives.FileInfo
import utils.configs
import utils.configs.bucketsPath
import s3.organizer.implementations.{Bucket, Repository}


object Service {

  def fileDestination(fileInfo: FileInfo)(implicit organizers: RepositoryMetadata): File =
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
                     ): RepositoryMetadata =

    val _repository = repository match
      case Some(value) => value
      case _ => null
    
    val _versioned = versioned match
      case Some(value) => value
      case _ => false

  
    val _version = version match
      case Some(value) => value
      case _ => 1.0f
      
    RepositoryMetadata(
      bucket=bucket,
      repository=_repository,
      versioned=_versioned,
      version=_version
    )
}
