package pedro.goncalves
package api.services


import s3.organizer.bucket.listBuckets
import api.models
import scala.concurrent.Future


object Bucket:
  
  import scala.concurrent.ExecutionContext.Implicits.global

  def jsonBuckets: Future[models.Buckets] =
    listBuckets.map { buckets =>
    models.Buckets(buckets.map(bucket => models.Bucket(name=bucket.name)))
    }
