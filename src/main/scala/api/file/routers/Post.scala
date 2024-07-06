package pedro.goncalves
package api.file.routers


import java.io.File
import scala.util.{Failure, Success}

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.directives.FileInfo

import api.file.Service.{completeStorage, fileDestination}
import api.file.RepositoryMetadata
import api.auth.Service.endpointAuthenticator
import api.file.exceptions.InconsistentRepositoryVersion
import api.file.exceptions.InconsistentParameters
import api.repository.exceptions.{RepositoryExists, RepositoryNotExists}
import api.bucket.exceptions.BucketNotExists
import s3.organizer.bucket.Bucket
import s3.organizer.repository.Repository
import s3.structured.CSVFile


class Post extends Directives:

  import scala.concurrent.ExecutionContext.Implicits.global

  val route: Route = authenticateOAuth2(realm = "secure site", endpointAuthenticator) { auth =>
    authorize(true) { // TODO: Implement authorization logic
      post {
        parameter(
          "bucket".as[String],
          "repository".as[String].optional,
          "versioned".as[Boolean].optional,
          "version".as[Float].optional,
          "create".as[Boolean].optional
        ) { (bucketName, rawRepositoryName, rawVersioned, rawVersion, createRepository) =>

          val treatedParameters = completeStorage(bucketName, rawRepositoryName, rawVersioned, rawVersion)

          val createIfNotExists: Boolean = createRepository match
            case Some(value) => value
            case _ => false

          if (treatedParameters.versioned && rawRepositoryName.orNull == null)
            throw new InconsistentParameters

          val bucketOperations = Bucket(bucketName)

          if (!bucketOperations.check)
            throw BucketNotExists(bucketName)

          val repository = Repository(bucketOperations, treatedParameters.repository)

          if (!repository.check && !createIfNotExists)
            throw RepositoryNotExists(repository.name)

          val lastVersion = repository.lastVersion
          if (lastVersion >= treatedParameters.version)
            throw InconsistentRepositoryVersion(lastVersion)

          implicit val organizers: RepositoryMetadata = treatedParameters

          storeUploadedFile("file", fileDestination) {
            case (metadata, file) =>

              val organizerPath = file.getPath.split("\\\\").dropRight(1).mkString("\\")
              val structuredFile = CSVFile(file.getPath, metadata.getFileName, organizerPath)

              if (createIfNotExists)

                onComplete(repository.create) {
                  case Success(_) =>
                    onComplete(structuredFile._generate) {
                      case Success(_) => complete(StatusCodes.OK)
                      case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
                    }
                  case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
                }

              else
                onComplete(structuredFile._generate) {
                  case Success(_) => complete(StatusCodes.OK)
                  case Failure(exception) => complete(StatusCodes.InternalServerError, exception.getMessage)
                }
          }
        }
      }
    }
  }


