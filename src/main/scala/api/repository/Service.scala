package pedro.goncalves
package api.repository


import scala.collection.mutable
import scala.concurrent.Future

import api.repository
import s3.organizer.implementations
import s3.organizer
import s3.organizer.implementations.listBuckets
import s3.organizer.implementations.Repository as RepositoryMetadata
import api.bucket.models.Bucket as BucketJson


object Service:
  import scala.concurrent.ExecutionContext.Implicits.global

  def jsonRepositories(bucket: Option[BucketJson]): Future[Repositories] =
  bucket match {
    case Some(bc) =>
      
        val bucket = implementations.Bucket(bc.name)

        val repositories: Future[Seq[RepositoryMetadata]] =
          bucket.listAllRepositories

        val futureRepositories = repositories.flatMap { repositories =>
          val futureUpdatedRepositories = Future.sequence(
            repositories.map { repo =>
              repo._getID.map {
                case Some(id) => api.repository.Repository(
                  id = Some(id),
                  bucket_name = repo.bucket.name,
                  name = repo.name,
                  versioned = repo.versioned
                )
                case None => api.repository.Repository(
                  id = Some(""),
                  bucket_name = repo.bucket.name,
                  name = repo.name,
                  versioned = repo.versioned
                )
              }
            }
          )
          futureUpdatedRepositories.map(updatedRepositories =>
            api.repository.Repositories(updatedRepositories.toArray)
          )
        }
        futureRepositories
    case None =>
      listBuckets.flatMap { buckets =>
  
        val repositories: Array[Future[Seq[RepositoryMetadata]]] =
          buckets.map(_.listAllRepositories)
  
        val futureArrayOfRepositories: Future[mutable.ArraySeq[RepositoryMetadata]] =
          Future.sequence(repositories).map(_.flatten)
  
        val futureRepositories = futureArrayOfRepositories.flatMap { repositories =>
          val futureUpdatedRepositories = Future.sequence(
            repositories.map { repo =>
              repo._getID.map {
                case Some(id) => api.repository.Repository(
                  id = Some(id),
                  bucket_name = repo.bucket.name,
                  name = repo.name,
                  versioned = repo.versioned
                )
                case None => api.repository.Repository(
                  id = Some(""),
                  bucket_name = repo.bucket.name,
                  name = repo.name,
                  versioned = repo.versioned
                )
              }
            }
          )
          futureUpdatedRepositories.map(updatedRepositories =>
            api.repository.Repositories(updatedRepositories.toArray)
          )
        }
        futureRepositories
      }
  }

