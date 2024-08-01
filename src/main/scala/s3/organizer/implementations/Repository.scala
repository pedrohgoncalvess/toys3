package pedro.goncalves
package s3.organizer.implementations


import java.io.File
import java.util.UUID
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.*

import org.json4s.JsonAST.JObject

import s3.metadata.implementations.Repository as RepositoryMetadata
import s3.organizer.Organizer
import utils.configs.bucketsPath


case class Repository(
                     bucket:Bucket,
                     name:String,
                     versioned:Boolean=false
                       )
  extends RepositoryMetadata(
    bucket=bucket,
    name=name,
    versioned=versioned
  )
    with Organizer:

  import scala.concurrent.ExecutionContext.Implicits.global
  
  override val organizerPath = s"$bucketsPath\\${bucket.name}\\$name"

  def create(c: Option[JObject]): Future[Unit] =
      File(organizerPath).mkdir()
      val generateRepo = this._createDefaultMetadata(c)
      Await.result(generateRepo, 5.seconds)

      val repoID: Future[Option[String]] = this._getID
      repoID.flatMap {
        case Some(id) =>
          bucket.addRepository(UUID.fromString(id))
        case None =>
          this.exclude
          Future.failed(new Exception("Can't add repository ID in metadata.json of bucket."))
        }


  def exclude: Future[Unit] =
    val repository = File(organizerPath)

    val repoID: Future[Option[String]] = this._getID
    repoID.map{
      case Some(id) =>
        bucket.remRepository(UUID.fromString(id)).flatMap { _ =>
          repository.listFiles().foreach(_.delete())
          repository.delete()
          Future.successful(())
        }
      case None =>
        Future.failed(new Exception("Can't remove repository ID in metadata.json of bucket."))
    }


  def lastVersion: Float =
    val dirs = File(organizerPath).listFiles.filter(!_.isFile)
    if (dirs.isEmpty)
      0f
    else
      dirs.map(_.getName.replace("v"/*v is a signature of versioned folder*/,"").toFloat).max
  
  def createVersion(lastVersion:Float): Unit =
    File(s"$organizerPath\\v${lastVersion+1}").mkdir()

  def check: Boolean =
      File(organizerPath).exists()
