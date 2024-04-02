package pedro.goncalves
package s3.organizer.repository


import s3.metadata
import s3.organizer.bucket.{Bucket, listUUIDs}
import utils.configs.bucketsPath
import org.json4s.JsonAST.JObject
import org.json4s.{JBool, JField, JInt, JString}
import s3.utils.generateNewUUID
import scala.concurrent.Future


class Metadata(
                bucket:Bucket,
                repositoryName:String,
                versioned:Boolean
              ) extends metadata.Metadata:

  override val metadataPath:String = s"$bucketsPath\\${bucket.bucketName}\\$repositoryName\\$metadataFileName"
  
  import scala.concurrent.ExecutionContext.Implicits.global

  override def _content: Future[JObject] =
    listUUIDs.map { fUuids =>

      JObject(
        "id" -> JString(generateNewUUID(fUuids)),
        "created_at" -> JString(java.time.LocalDateTime.now().toString),
        "created_by" -> JString("admin"),
        "objects" -> JInt(0),
        "active" -> JBool(true),
        "versioned" -> JBool(versioned)
      )
    }

