package pedro.goncalves
package s3.metadata


import java.io.File
import java.io.FileWriter
import java.util.UUID
import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.Using
import scala.util.{Failure, Success}

import org.json4s.{JField, JString, JValue}
import org.json4s.JsonAST.{JBool, JObject}
import org.json4s.native.JsonMethods.{compact, parse, render}


/**
 * An interface for implementing the creation, removal, and editing of object and organizer metadata files.
 */
trait Metadata:
  
  val metadataFileName:String = ".metadata.json"
  val metadataPath:String

  def _content(externalMetadata: Option[JObject]): Future[JObject]

  def _createDefaultMetadata(externalMetadata: Option[JObject])(implicit ex: ExecutionContext): Future[Unit] =
    val content: Future[JObject] = this._content(externalMetadata)
    content.flatMap { value =>
      this._create(value)
    }
    
  def _exists: Boolean =
    File(metadataPath).exists

  def _getID(implicit ex: ExecutionContext): Future[Option[String]] =
    if (this._exists)
      _read.map( content =>
        content.get("id") match
          case Some(JString(id)) => Some(id)
          case _ => throw Exception("")
      )
    else
      Future.failed(Exception("Metadata file of repository not exists."))

      
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
      
  /**
   *a function that rewrites the .metadata.json file with the new JObject provided.
   * 
   * @param content a JObject with the content to be written to the file
   */
  def _create(content: JObject)(implicit ex: ExecutionContext): Future[Unit] =
    Future:
      val jsonString = compact(render(content))

      Using(new FileWriter(metadataPath)) { writer =>
        writer.write(jsonString)
      }
  
  
  //TODO: Record of the user who deactivated the bucket
  def _changeStatus(implicit ex: ExecutionContext, status:Boolean, userID:UUID): Future[Unit] =
    val jsonStringOpr = Using(Source.fromFile(metadataPath)) { source => source.mkString }
    val jsonString: String = jsonStringOpr match {
      case Success(content: String) => content
      case Failure(exception) => throw new Exception(exception.getMessage)
    }

    val parsedJson = parse(jsonString)
    val updatedJson = parsedJson match
      case JObject(fields) =>
        JObject(fields.map {
          case ("active", _) => ("active", JBool(status))
          case otherField => otherField
        })
      case _ => throw new Exception("Error updating metadata.json")

    val finalJson = if (status)
      updatedJson.merge(JObject("activated_at" -> JString(LocalDateTime.now.toString)))
      updatedJson.merge(JObject("status_changed_by" -> JString(userID.toString)))
    else
      updatedJson.merge(JObject("deactivated_at" -> JString(LocalDateTime.now.toString)))
      updatedJson.merge(JObject("status_changed_by" -> JString(userID.toString)))

    this._create(finalJson)

  
