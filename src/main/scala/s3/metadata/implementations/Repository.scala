package pedro.goncalves
package s3.metadata.implementations

import s3.metadata
import utils.configs.bucketsPath

import org.json4s.JsonAST.JObject
import org.json4s.{JBool, JField, JInt, JString}
import pedro.goncalves.s3.organizer.implementations.Bucket

import java.util.UUID
import scala.concurrent.Future


class Repository(
                bucket:Bucket,
                name:String,
                versioned:Boolean
              ) extends metadata.Metadata:

  override val metadataPath:String = s"$bucketsPath\\${bucket.name}\\$name\\$metadataFileName"
  
  import scala.concurrent.ExecutionContext.Implicits.global

  override def _content(externalMetadata:Option[JObject]): Future[JObject] =
    Future:
        val defaultMetadata = JObject(
          "id" -> JString(UUID.randomUUID.toString),
          "created_at" -> JString(java.time.LocalDateTime.now().toString),
          "created_by" -> JString("admin"),
          "objects" -> JInt(0),
          "active" -> JBool(true),
          "versioned" -> JBool(versioned)
        )
        if externalMetadata.orNull != null then defaultMetadata.merge(externalMetadata.get) else defaultMetadata



