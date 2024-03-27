package pedro.goncalves
package s3.organizer.bucket


import org.json4s.{JBool, JInt, JString}
import org.json4s.JsonAST.JObject
import org.json4s.native.JsonMethods.*
import s3.metadata
import scala.util.{Failure, Success}
import scala.concurrent.Future
import scala.io.Source
import scala.util.Using


class Metadata(bucketName:String) extends metadata.Metadata {

  import utils.configs.bucketsPath
  import scala.concurrent.ExecutionContext.Implicits.global
  
  override val metadataPath:String = s"$bucketsPath\\$bucketName\\$metadataFileName"

  /**
   * method used only by the create method that creates the bucket
   *
   * @return future unit with the result of the operation
   */
  override def _generate: Future[Unit] =
    val content: Future[JObject] = this._content
    content.flatMap{ value =>
      this._create(value)
    }
  
  override def _content: Future[JObject] =
    Future:
      JObject(
        "created_at" -> JString(java.time.LocalDateTime.now().toString),
        "created_by" -> JString("admin"),
        "objects" -> JInt(0),
        "active" -> JBool(true),
        "repositories" -> JInt(0)
      )
  

  override def _disability: Future[Unit] =
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

    this._create(updatedJson)


  override def _read: Future[Map[String, Any]] = ???
}
