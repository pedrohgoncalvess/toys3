package pedro.goncalves
package s3.metadata.implementations


import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Failure, Success, Using}
import scala.concurrent.ExecutionContext.Implicits.global

import org.json4s.JsonAST.{JArray, JNull, JObject}
import org.json4s.native.JsonMethods.*
import org.json4s.{JBool, JInt, JString, JValue}
import s3.metadata
import utils.configs.bucketsPath


abstract class Bucket(name:String) extends metadata.Metadata:

  override val metadataPath: String = s"$bucketsPath\\$name\\$metadataFileName"
  
  override def _content(externalMetadata:Option[JObject]): Future[JObject] =
    Future:
      val defaultMetadata = JObject(
        "id" -> JString(UUID.randomUUID.toString),
        "created_at" -> JString(java.time.LocalDateTime.now().toString),
        "created_by" -> JString("admin"),
        "objects" -> JInt(0),
        "active" -> JBool(true),
        "repositories" -> JArray(List(JNull)),
      )
      if externalMetadata.orNull != null then defaultMetadata.merge(externalMetadata.get) else defaultMetadata
      
  private def read: Future[JObject] =
    Future:
      val jsonStringOpr = Using(Source.fromFile(metadataPath)) { source => source.mkString }
  
      val jsonString: String = jsonStringOpr match {
        case Success(content) => content
        case Failure(exception) => throw new Exception(s"Error reading .metadata.json. \nReason: ${exception.getMessage}")
      }
    
      val parsedJson = parse(jsonString)
      parsedJson match 
        case JObject(fields) => JObject(fields)
        case null => null
        
  def listIDRepositories: Future[Seq[UUID]] =
    val IDStrings = read.map {
      case JObject(fields) =>
        fields.collectFirst {
          case ("repositories", JArray(values)) =>
            values.collect {
              case JString(repo) => repo
            }
        }
    }
    IDStrings.map {
      case Some(values) => values.map(id => UUID.fromString(id))
      case None => throw Exception("")
    }
        

  def addRepository(repositoryID: UUID): Future[Unit] =
    val updatedJson = read.map{
        case JObject(fields) =>
          val updatedFields = fields.map {
            case ("repositories", value: JArray) =>
              ("repositories", if value.arr.head == JNull then JArray(List(JString(repositoryID.toString))) else JArray(value.arr :+ JString(repositoryID.toString)))
            case ("objects", value: JInt) => ("objects", JInt(value.values + 1))
            case otherField => otherField
          }
          JObject(updatedFields)
        case _ => throw new Exception("Error parsing metadata.json.")
      }

    updatedJson.flatMap { updatedJson =>
      this._create(updatedJson)
    }
    
      
  def remRepository(repositoryID: UUID): Future[Unit] =
    val updatedJson = read.map{
        case JObject(fields) =>
          val updatedFields = fields.map {
            case ("repositories", value: JArray) =>
              ("repositories",
                if value.arr.head == JNull then throw Exception("Not exists repositories.")
                else if value.arr.length == 1 then JArray(List(JNull))
                else JArray(value.arr.filter {
                  case repo: JObject =>
                    val repoString = compact(render(repo))
                    repoString != repositoryID.toString
                  case _ => false
                }))
            case ("objects", value:JInt) => ("objects", JInt(value.values - 1))
            case otherField => otherField
          }
          JObject(updatedFields)
        case _ => throw new Exception("Error parsing metadata.json.")
      }

    updatedJson.flatMap { updatedJson =>
      this._create(updatedJson)
    }