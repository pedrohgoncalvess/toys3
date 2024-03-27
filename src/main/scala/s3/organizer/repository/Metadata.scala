package pedro.goncalves
package s3.organizer.repository


import s3.metadata
import s3.organizer.bucket.Bucket
import utils.configs.bucketsPath
import org.json4s.JsonAST.JObject
import org.json4s.{JBool, JInt, JString}
import scala.concurrent.Future


class Metadata(
                bucket:Bucket,
                repositoryName:String
              ) extends metadata.Metadata {

  override val metadataPath:String = s"$bucketsPath\\${bucket.bucketName}\\$repositoryName\\$metadataFileName"
  
  import scala.concurrent.ExecutionContext.Implicits.global

  override def _content: Future[JObject] =
    Future:
      JObject(
        "created_at" -> JString(java.time.LocalDateTime.now().toString),
        "created_by" -> JString("admin"),
        "objects" -> JInt(0),
        "active" -> JBool(true),
      )

  override def _generate: Future[Unit] =
    val content: Future[JObject] = this._content
    content.flatMap { value =>
      this._create(value)
    }


  //TODO: Implement this interfaces
  override def _read: Future[Map[String, Any]] = ???
  override def _disability: Future[Unit] = ???
}
