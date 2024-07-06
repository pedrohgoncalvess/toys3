package pedro.goncalves
package api.repository


import scala.collection.mutable
import scala.concurrent.Future

import api.repository
import api.bucket.Bucket as BucketJson
import s3.organizer
import s3.organizer.bucket.listBuckets


object Service:
  import scala.concurrent.ExecutionContext.Implicits.global

  def jsonRepositories(bucket: BucketJson): Future[Repositories] =
    listBuckets.flatMap { buckets =>

      val repositories: Array[Future[Array[organizer.repository.Repository]]] =
        buckets.map(_.listRepositories)

      val futureArrayOfRepositories: Future[mutable.ArraySeq[organizer.repository.Repository]] =
        Future.sequence(repositories).map(_.flatten)
  
      futureArrayOfRepositories.map { repositories =>
        val updatedRepositories = repositories.map(repo =>
          api.repository.Repository(
            bucket_name = repo.bucket.name,
            name = repo.name,
            versioned = repo.versioned
          )
        )
        api.repository.Repositories(updatedRepositories.toArray)
      }
    }
