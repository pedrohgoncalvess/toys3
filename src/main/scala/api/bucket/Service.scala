package pedro.goncalves
package api.bucket


import scala.collection.mutable
import scala.concurrent.Future

import s3.organizer
import s3.organizer.implementations.listBuckets
import api.bucket.models.{Buckets, Bucket}


object BucketService:
  
  import scala.concurrent.ExecutionContext.Implicits.global

  def jsonBuckets: Future[Buckets] =
    listBuckets.flatMap { buckets =>
      val futureBuckets: Future[mutable.ArraySeq[Bucket]] = Future.sequence(
        buckets.map { bucket =>
          bucket._getID.map {
            case Some(id) => Bucket(id=id, name=bucket.name)
            case None => null
          }
        }
      )
      futureBuckets.map(bucketsSeq => Buckets(bucketsSeq.toSeq))
    }
