package pedro.goncalves
package s3.organizer.bucket


import utils.configs.bucketsPath
import org.json4s.native.JsonMethods.*
import org.json4s.*
import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import s3.organizer.Organizer
import s3.organizer.repository.Repository


/**
  * Class that represents bucket
  *
  * @param bucketName the name of bucket
  *
  */
case class Bucket (
                     name:String
                   ) 
  extends Metadata(name) with Organizer:

  override val organizerPath: String = s"$bucketsPath\\$name"
  
  def check: Boolean = File(organizerPath).exists()
  
  def create: Future[Unit] =
    File(organizerPath).mkdir()
    this._generate

  def exclude: Future[Unit] =
    Future {
      val bucket = File(organizerPath)
      val bucketRepositories = bucket.listFiles().filter(dir => !dir.isFile)
      bucketRepositories.foreach(_.listFiles().foreach(_.delete))
      bucket.listFiles.foreach(_.delete)
      bucket.delete()
    }

  def listRepositories: Future[Array[Repository]] =
    val metadataContent: Future[Map[String, JValue]] = _read

    metadataContent.map { content =>
      content.get("repositories") match {
        case Some(JArray(list)) => list.collect {
          case JString(repo) => Repository(bucket=this, name=repo)
        }.toArray
        case _ => Array.empty[Repository]
      }
    }


/**
 * a method outside the bucket class that lists all buckets, 
 * it's outside the bucket class because it's not a bucket, 
 * it's a top-level representation of the unnamed buckets that just lists them
 * 
 * @return list of buckets objects
 */

def listBuckets: Future[Array[Bucket]] =
  Future:
    val bucketsFile = File(bucketsPath).listFiles.filter(!_.isFile)
    bucketsFile.map(file => Bucket(file.getName))
