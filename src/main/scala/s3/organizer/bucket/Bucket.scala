package pedro.goncalves
package s3.organizer.bucket


import api.models
import utils.configs.bucketsPath
import org.json4s.native.JsonMethods.*
import org.json4s.*
import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import s3.organizer.Organizer


/**
  * Class that represents bucket
  *
  * @param bucketName the name of bucket
  *
  */
case class Bucket (
                     bucketName:String
                   ) extends Metadata(bucketName) with Organizer:

  override val organizerPath: String = s"$bucketsPath\\$bucketName"

  /**
   * A method that checks to see if that bucket exists
   *
   * @return true for exist and false for non exists
   */
  def check: Boolean = File(organizerPath).exists()


  /**
   * A method that create a bucket and a metadata file for this bucket
   * @return result of creation operation
   */
  def create: Future[Unit] =
    Future {
      File(organizerPath).mkdir()
      this._generate
    }

  /**
   * A method that exclude all files and repositories inside of bucket
   * @return result of delete operation
   */
  def exclude: Future[Unit] =
    Future {
      val bucket = File(organizerPath)
      val bucketRepositories = bucket.listFiles().filter(dir => !dir.isFile)
      bucketRepositories.foreach(_.listFiles().foreach(_.delete))
      bucket.listFiles.foreach(_.delete)
      bucket.delete()
    }


/**
 * a method outside the bucket class that lists all buckets, 
 * it's outside the bucket class because it's not a bucket, 
 * it's a top-level representation of the unnamed buckets that just lists them
 * 
 * @return list of buckets objects
 */
def listBuckets: Future[models.Buckets] =
  Future {
    models.Buckets(File(bucketsPath)
      .listFiles
      .map(bucket => models.Bucket(
        name = bucket.getName
        )
      )
    )
  }
