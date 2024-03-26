package pedro.goncalves
package api.services.organizers.buckets

import api.json
import utils.configs.bucketsPath
import org.json4s.native.JsonMethods.*
import org.json4s.*
import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


/**
  * Class that represents bucket
  *
  * @param bucketName the name of bucket
  *
  */
case class Buckets (
                     bucketName:String
                   ) extends MetadataBuckets(bucketName):

  /**
   * A method that checks to see if that bucket exists
   *
   * @return true for exist and false for non exists
   */
  def check: Future[Boolean] =
    Future {
      File(s"$bucketsPath\\$bucketName").exists()
    }


  /**
   * A method that create a bucket and a metadata file for this bucket
   * @return result of creation operation
   */
  def create: Future[Unit] =
    Future {
      File(s"$bucketsPath\\$bucketName").mkdir()
      this._create
    }

  /**
   * A method that exclude all files and repositories inside of bucket
   * @return result of delete operation
   */
  def exclude: Future[Unit] =
    Future {
      val bucket = File(s"$bucketsPath\\$bucketName")
      val bucketFiles = bucket.listFiles().filter(file => file.isFile)
      val bucketRepositories = bucket.listFiles().filter(dir => !dir.isFile)
      bucketFiles.foreach(_.delete)
      bucketRepositories.foreach(_.listFiles().foreach(_.delete))
      bucket.delete()
    }

  override def _disability: Future[Unit] = ???

  override def _read: Future[Map[String, Any]] = ???


/**
 * a method outside the bucket class that lists all buckets, 
 * it's outside the bucket class because it's not a bucket, 
 * it's a top-level representation of the unnamed buckets that just lists them
 * 
 * @return list of buckets objects
 */
def listBuckets: Future[json.Buckets] =
  Future {
    json.Buckets(File(bucketsPath)
      .listFiles
      .map(bucket => json.Bucket(
        name = bucket.getName
        )
      )
    )
  }
