package pedro.goncalves
package api.services


import s3.organizer.bucket.listBuckets
import api.models
import org.json4s.{JString, JValue}
import scala.collection.mutable
import scala.concurrent.Future


object Bucket {
  
  import scala.concurrent.ExecutionContext.Implicits.global

  def idNameBucket: Future[models.Buckets] =
    val buckets = listBuckets

    listBuckets.flatMap { buckets =>

      val bucketsJson: Array[Future[Array[models.Bucket]]] = buckets.map { bucket =>
        val metadataContent: Future[Map[String, JValue]] = bucket._read

        metadataContent.map { metadataList =>
          metadataList.collect {
            case ("id", value: JString) => models.Bucket(Some(value.values), bucket.bucketName)
          }.toArray
        }
      }

      val bucketsJsonFuture: Future[mutable.ArraySeq[models.Bucket]] = Future.sequence(bucketsJson).map { array =>
        array.flatten
      }

      bucketsJsonFuture.map { buckets =>
        val arrayBuckets = buckets.toArray
        models.Buckets(arrayBuckets)

      }
    }
}
