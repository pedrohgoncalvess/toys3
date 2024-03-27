package pedro.goncalves
package s3.organizer.repository


import s3.organizer.Organizer
import utils.configs.bucketsPath
import s3.organizer.bucket.Bucket
import java.io.File
import scala.concurrent.Future


case class Repository(
                       bucket:Bucket,
                       repositoryName:String
                       )
  extends Metadata(
    bucket=bucket,
    repositoryName=repositoryName
  )
    with Organizer:


  import scala.concurrent.ExecutionContext.Implicits.global

  if (!bucket.check)
    throw new Exception(s"Bucket ${bucket.bucketName} not exists.")
  
  override val organizerPath = s"$bucketsPath\\${bucket.bucketName}\\$repositoryName"

  override def create: Future[Unit] =
    Future:
      File(organizerPath).mkdir()

  override def exclude: Future[Unit] =
    Future {
      val repository = File(organizerPath)
      repository.listFiles().foreach(_.delete())
      repository.delete()
    }

  def lastVersion: Float =
    val dirs = File(organizerPath).listFiles.filter(!_.isFile)
    if (dirs.isEmpty)
      0f
    else
      dirs.map(_.getName.replace("v","").toFloat).max
  
  def createVersion(lastVersion:Float): Future[Unit] =
    Future:
      File(s"$organizerPath\\v${lastVersion+1}").mkdir()

  override def check: Boolean =
      File(organizerPath).exists()
