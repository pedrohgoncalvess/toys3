package pedro.goncalves
package api.services


import scala.concurrent.Future
import api.models
import api.models.Repositories
import s3.organizer.bucket.listBuckets
import s3.organizer
import scala.collection.mutable


object Repository:
  import scala.concurrent.ExecutionContext.Implicits.global

  def jsonRepositories(bucket: models.Bucket): Future[models.Repositories] =
    listBuckets.flatMap { buckets =>

      val repositories: Array[Future[Array[organizer.repository.Repository]]] =
        buckets.map(_.listRepositories)

      val futureArrayOfRepositories: Future[mutable.ArraySeq[organizer.repository.Repository]] =
        Future.sequence(repositories).map(_.flatten)
  
      futureArrayOfRepositories.map { repositories =>
        val updatedRepositories = repositories.map(repo =>
          models.Repository(
            bucket_name = repo.bucket.name,
            name = repo.name,
            versioned = false
          )
        )
        models.Repositories(updatedRepositories.toArray)
      }
    }

