package pedro.goncalves
package s3.metadata


import org.json4s.{JField, JString, JValue}
import org.json4s.JsonAST.{JBool, JObject}
import org.json4s.native.JsonMethods.{compact, parse, render}
import java.io.File
import java.io.FileWriter
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source
import scala.util.Using
import scala.util.{Failure, Success}


/**
 * An interface for implementing the creation, removal, and editing of object and organizer metadata files
 */
trait Metadata:
  
  val metadataFileName:String = ".metadata.json"
  val metadataPath:String

  def _content: Future[JObject]
  
  def _generate(implicit ex: ExecutionContext): Future[Unit] =
    val content: Future[JObject] = this._content
    content.map { value =>
      this._create(value)
    }
    
  def _exists: Boolean =
    File(metadataPath).exists

  def _read(implicit ex: ExecutionContext): Future[Map[String, JValue]] =
    Future:
      val jsonStringOpr = Using(
        Source.fromFile(metadataPath)
      ) { source => source.mkString }

      val jsonString: String = jsonStringOpr match {
        case Success(content) => content
        case Failure(exception) => throw new Exception(s"Error reading metadata file.")
      }

      val parsedJson = parse(jsonString)
      val jsonToMap: Map[String, JValue] = parsedJson match {
        case JObject(fields) => fields.toMap
        case _ => throw new Exception
      }

      jsonToMap
      
  
  def _create(content: JObject)(implicit ex: ExecutionContext): Future[Unit] =
    Future:
      val jsonString = compact(render(content))

      Using(new FileWriter(metadataPath)) { writer =>
        writer.write(jsonString)
      }
  
  
  //TODO: Record of the user who deactivated the bucket
  def _disability(implicit ex: ExecutionContext): Future[Unit] =
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
      case _ => throw new Exception

    this._create(updatedJson)
  
