package pedro.goncalves
package api.services.organizers.repositorys

import api.services
import api.services.organizers.buckets.Buckets
import utils.configs.bucketsPath

import java.io.File
import scala.concurrent.Future
import scala.util.{Failure, Success}


case class Repository(
                       bucket:Buckets,
                       repositoryName:String
                ):

  import scala.concurrent.ExecutionContext.Implicits.global

  bucket.check.map(result =>
    if (!result)
      throw new Exception(s"Bucket ${bucket.bucketName} not exists."))
    .onComplete {
      case Success(_) =>
      case Failure(exception) => throw exception
    }
  
  val repositoryPath = s"$bucketsPath\\${bucket.bucketName}\\$repositoryName"

  def create: Unit =
    File(repositoryPath).mkdir()

  def delete(bucketName:String, repositoryName:String): Future[Unit] =
    Future {
      val repository = File(repositoryPath)
      repository.listFiles().foreach(_.delete())
      repository.delete()
    }