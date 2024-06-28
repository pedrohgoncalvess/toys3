package pedro.goncalves
package s3.organizer.bucket


import org.json4s.{JBool, JInt, JString, JValue}
import org.json4s.JsonAST.{JArray, JNull, JObject}
import org.json4s.native.JsonMethods.*
import s3.metadata
import java.util.UUID
import scala.util.{Failure, Success, Using}
import scala.concurrent.Future
import scala.io.Source


class Metadata(bucketName:String) extends metadata.Metadata:

  import utils.configs.bucketsPath
  import scala.concurrent.ExecutionContext.Implicits.global

  override val metadataPath: String = s"$bucketsPath\\$bucketName\\$metadataFileName"
  

  override def _content: Future[JObject] =
    Future:
      JObject(
        "id" -> JString(UUID.fromString.toString),
        "created_at" -> JString(java.time.LocalDateTime.now().toString),
        "created_by" -> JString("admin"),
        "objects" -> JInt(0),
        "active" -> JBool(true),
        "repositories" -> JArray(List(JNull))
      )

  def addRepository(repositoryName: String): Future[Unit] =
    Future:
      val jsonStringOpr = Using(Source.fromFile(metadataPath)) { source => source.mkString }

      val jsonString: String = jsonStringOpr match {
        case Success(content) => content
        case Failure(exception) => throw new Exception(s"Error reading .metadata.json.")
      }

      val parsedJson = parse(jsonString)
      val updatedJson = parsedJson match {
        case JObject(fields) =>
          val updatedFields = fields.map {
            case ("repositories", value: JArray) =>
              ("repositories", if value.arr.head == JNull then JArray(List(JString(repositoryName))) else JArray(value.arr :+ JString(repositoryName)))
            case otherField => otherField
          }
          JObject(updatedFields)
        case _ => throw new Exception("Error parsing metadata.json.")
      }

      this._create(updatedJson)

