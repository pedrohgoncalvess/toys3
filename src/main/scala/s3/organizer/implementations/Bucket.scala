package pedro.goncalves
package s3.organizer.implementations


import s3.metadata.implementations.Bucket as BucketMetadata
import s3.organizer.Organizer
import utils.configs.bucketsPath

import org.json4s.*
import org.json4s.native.JsonMethods.*

import java.io.File
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}


/**
  * Class that represents bucket
  *
  * @param bucketName the name of bucket
  *
  */
case class Bucket (
                  name:String
                   ) 
  extends BucketMetadata(name) with Organizer:

  override val organizerPath: String = s"$bucketsPath\\$name"
  
  def check: Boolean =
    val metadataCheck = this._exists
    File(organizerPath).exists() && metadataCheck
  
  
  def create(c:Option[JObject]): Future[Unit] =
    File(organizerPath).mkdir()
    this._createDefaultMetadata(c)

  
  def exclude: Future[Unit] =
    Future {
      val bucket = File(organizerPath)
      val bucketRepositories = bucket.listFiles().filter(dir => !dir.isFile)
      bucketRepositories.foreach(_.listFiles().foreach(_.delete))
      bucket.listFiles.foreach(_.delete)
      bucket.delete()
    }

  
  def listAllRepositories: Future[Seq[Repository]] =
    listIDRepositories.flatMap { ids =>
      val repositoryFutures: Seq[Future[Option[Repository]]] = ids.map(getRepositoryByID)
      Future.sequence(repositoryFutures).map(_.flatten)
    }


  def getRepositoryByID(repositoryID: UUID): Future[Option[Repository]] =
    val bucket = new File(organizerPath)
    val bucketDirs = bucket.listFiles().filter(dir => dir.isDirectory)
    val bucketRepositories = bucketDirs.map(dir => Repository(bucket = this, name = dir.getName))

    val matchingRepositories = bucketRepositories.map { repo =>
      repo._getID.map {
        case Some(id) if id == repositoryID.toString => Some(repo)
        case _ => None
      }
    }

    Future.sequence(matchingRepositories).map(_.flatten.headOption)


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
    bucketsFile.map(file => Bucket(file.getName)).filter(_.check)
