package pedro.goncalves
package api.services.objects

import java.io.File
import utils.configs.bucketsPath
import scala.concurrent.Future
import api.services
import scala.util.{Success, Failure}


class Repository(
                bucket:services.Buckets,
                repository:String
                ):

  import scala.concurrent.ExecutionContext.Implicits.global

  bucket.check.map(result =>
    if (!result)
      throw new Exception(s"Bucket ${bucket.bucketName} not exists."))
    .onComplete {
      case Success(_) =>
      case Failure(exception) => throw exception
    }

  def create: Unit =
    File(s"$bucketsPath\\${bucket.bucketName}\\$repository").mkdir()

  def delete(bucketName:String, repositoryName:String): Future[Unit] =
    Future {
      val repository = File(s"$bucketsPath\\$bucketName\\$repositoryName")
      repository.listFiles().foreach(_.delete())
      repository.delete()
    }