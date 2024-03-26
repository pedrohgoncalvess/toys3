package pedro.goncalves
package api.services.organizers.buckets

import org.json4s.{JBool, JInt, JString}
import org.json4s.JsonAST.JObject
import org.json4s.native.JsonMethods.*
import pedro.goncalves.api.services.metadata.Metadata
import scala.util.{Failure, Success}
import java.io.FileWriter
import scala.concurrent.Future
import scala.io.Source
import scala.util.Using


class MetadataBuckets(bucketName:String) extends Metadata {

  import utils.configs.bucketsPath
  import scala.concurrent.ExecutionContext.Implicits.global
  
  override val metadataPath:String = s"$bucketsPath\\$bucketName\\$metadataFileName"

  /**
   * method used only by the create method that creates the bucket
   *
   * @return future unit with the result of the operation
   */
  override def _create: Future[Unit] =
    Future:
      val content = JObject(
        "created_at" -> JString(java.time.LocalDateTime.now().toString),
        "created_by" -> JString("admin"),
        "objects" -> JInt(0),
        "active" -> JBool(true),
        "repositories" -> JInt(0)
      )
      val jsonString = compact(render(content))
      Using(new FileWriter(metadataPath)) { writer =>
        writer.write(jsonString)
      }


  override def _disability: Future[Unit] =
    Future:
      val jsonStringOpr = Using(Source.fromFile(metadataPath)) { source => source.mkString }
      val jsonString: String = jsonStringOpr match {
        case Success(content: String) => content
        case Failure(exception) => throw new Exception(exception.getMessage)
      }
  
      val parsedJson = parse(jsonString)
      val updatedJson = parsedJson match
        case JObject(fields) =>
          JObject(fields.map {
            case (name, value) if name == "active" => (name, JBool(false))
            case otherField => otherField
          })
  
      val updatedJsonString = compact(render(updatedJson))
      
      Using(new FileWriter(metadataPath)) { writer =>
        writer.write(updatedJsonString)
      }


  override def _read: Future[Map[String, Any]] = ???
}
