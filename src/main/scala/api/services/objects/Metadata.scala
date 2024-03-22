package pedro.goncalves
package api.services.objects

import org.json4s.JObject

import java.io.FileWriter
import scala.concurrent.Future
import org.json4s.native.JsonMethods.*
import utils.configs.bucketsPath
import org.json4s.*
import scala.util.{Failure, Success, Using}
import scala.io.Source

class Metadata(
               bucket:String,
               repository:String=null
               ):
  
  private val fileName:String = "_metadata.json"
  
  import scala.concurrent.ExecutionContext.Implicits.global
  
  private val metadataPath:String = this.repository match
    case repository:String => s"$bucketsPath\\$bucket\\$repository\\${this.fileName}"
    case null => s"$bucketsPath\\$bucket\\${this.fileName}"


  def create(content:JObject): Future[Unit] =
    Future:
      
      val jsonString = compact(render(content))
      val jsonWriter = new FileWriter(this.metadataPath)
      
      jsonWriter.write(jsonString)
      jsonWriter.close()
  
      
  def softDelete: Future[Unit] =
    Future:
      val jsonStringOpr = Using(Source.fromFile(this.metadataPath)) { source => source.mkString }
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
      val jsonWriter = new FileWriter(this.metadataPath)
      jsonWriter.write(updatedJsonString)
      jsonWriter.close()

