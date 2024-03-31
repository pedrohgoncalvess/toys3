package pedro.goncalves
package api.controllers.file


import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.directives.FileInfo

import java.io.File
import s3.organizer.bucket.Bucket
import s3.organizer.repository.Repository
import s3.structured.CSVFile

import scala.util.{Failure, Success}
import api.services.File.{completeStorage, fileDestination}
import api.models.FileStorage
import api.exceptions.file.fileExceptionHandler
import api.exceptions.file.{InconsistentParameters, InconsistentRepositoryVersion}
import api.exceptions.bucket.BucketNotExists
import api.exceptions.repository.RepositoryNotExists


class main extends Directives:

  val route: Route = pathPrefix("file") {
    concat(
        post {
          handleExceptions(fileExceptionHandler) {
            parameter(
              "bucket".as[String],
              "versioned".as[Boolean].optional,
              "repository".as[String].optional,
              "version".as[Float].optional,
              "create".as[Boolean].optional
            ) { (bucket, rawVersioned, rawRepository, rawVersion, createRepository) =>

              val treatedParameters = completeStorage(bucket, rawRepository, rawVersioned, rawVersion)

              val createIfNotExists:Boolean = createRepository match
                case Some(value) => value
                case _ => false

              if (treatedParameters.versioned && rawRepository.orNull == null)
                throw new InconsistentParameters

              val bucketOperations = Bucket(bucket)

              if (!bucketOperations.check)
                throw BucketNotExists(bucket)

              val repository = Repository(bucketOperations, treatedParameters.repository)

              if (!repository.check)
                if (createIfNotExists)
                  repository.create
                else
                  throw RepositoryNotExists(repository.repositoryName)

              val lastVersion = repository.lastVersion
              if (lastVersion >= treatedParameters.version)
                throw InconsistentRepositoryVersion(lastVersion)

              implicit val organizers: FileStorage = treatedParameters

              storeUploadedFile("file", fileDestination) {
                case (metadata, file) =>
                  val fileMetadata = CSVFile(repository, file.getPath, treatedParameters.versioned, treatedParameters.version)
                  onComplete(fileMetadata._generate) {
                    case Success(_) => complete(StatusCodes.OK)
                    case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
              }
            }
          }
        }
      }
    )
  }


