package pedro.goncalves
package pedro.goncalves.api.bucket

import s3.organizer
import s3.organizer.bucket.listBuckets

import scala.concurrent.Future


object BucketService:
  
  import scala.concurrent.ExecutionContext.Implicits.global

  def jsonBuckets: Future[Buckets] =
    listBuckets.map { buckets =>
      Buckets(buckets.map(bucket => Bucket(name=bucket.name)))
    }
