package pedro.goncalves
package s3.metadata

import org.json4s.JsonAST.JObject
import org.json4s.native.JsonMethods.{compact, render}

import java.io.FileWriter
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Using


/**
 * An interface for implementing the creation, removal, and editing of object and organizer metadata files
 */
trait Metadata {
  
  val metadataFileName:String = "_metadata.json"
  val metadataPath:String
  def _create(content: JObject)(implicit ex: ExecutionContext): Future[Unit] =
    Future:
      val jsonString = compact(render(content))
  
      Using(new FileWriter(metadataPath)) { writer =>
        writer.write(jsonString)
      }
      
  def _generate: Future[Unit]

  def _content: Future[JObject]
  
  def _read: Future[Map[String, Any]]
  
  //TODO: Record of the user who deactivated the bucket
  def _disability: Future[Unit] = ???
  
}
