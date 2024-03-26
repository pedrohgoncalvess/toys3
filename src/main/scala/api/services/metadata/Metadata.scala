package pedro.goncalves
package api.services.metadata

import org.json4s.JsonAST.JObject
import scala.concurrent.Future


/**
 * An interface for implementing the creation, removal, and editing of object and organizer metadata files
 */
trait Metadata {
  
  val metadataFileName:String = "_metadata.json"
  val metadataPath:String
  
  def _create: Future[Unit]
  
  def _read: Future[Map[String, Any]]
  
  //TODO: Record of the user who deactivated the bucket
  def _disability: Future[Unit] = ???
  
}
