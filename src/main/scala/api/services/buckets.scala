package pedro.goncalves
package api.services

import utils.configs.bucketsPath
import api.json.{Bucket, Buckets}

import org.json4s.{JInt, JString}
import org.json4s.JsonAST.JObject
import org.json4s.*
import org.json4s.native.JsonMethods.*

import scala.concurrent.Future
import java.io.{File, FileWriter}
import scala.io.Source
import scala.util.Using
import scala.util.{Success, Failure}


object buckets {

  import scala.concurrent.ExecutionContext.Implicits.global
  
  val bucketsDir = bucketsPath
  
  def checkBucket(bucketName: String): Future[Boolean] =
    Future {
      File(s"$bucketsDir\\$bucketName").exists()
    }

  private def createMetadataBucket(bucketName: String): Future[Unit] =
    Future {
      val json = JObject(
        "created_at" -> JString(java.time.LocalDateTime.now().toString),
        "created_by" -> JString("admin"),
        "objects" -> JInt(0),
        "active" -> JBool(true)
      )
      val jsonString = compact(render(json))
      val jsonWriter = new FileWriter(s"$bucketsDir\\$bucketName\\_metadata.json")
      jsonWriter.write(jsonString)
      jsonWriter.close()
    }
    
  def createBucket(bucketName: String): Future[Unit] =
    Future {
      File(s"$bucketsDir\\$bucketName").mkdir()
      createMetadataBucket(bucketName)
    }

  def softDeleteBucket(bucketName: String): Future[Unit] =
    Future {
      val filePath = s"$bucketsDir\\$bucketName\\_metadata.json"
      val jsonStringOpr = Using(Source.fromFile(filePath)) {source => source.mkString}
      val jsonString:String = jsonStringOpr match {
        case Success(content:String) => content
        case Failure(exception) => throw new Exception(exception.getMessage)
      }
      val json = parse(jsonString)

      val updatedJson = json match {
        case JObject(fields) =>
          JObject(fields.map {
            case (name, value) if name == "active" => (name, JBool(false))
            case otherField => otherField
          })
      }
      val updatedJsonString = compact(render(updatedJson))
      val jsonWriter = new FileWriter(filePath)
      jsonWriter.write(updatedJsonString)
      jsonWriter.close()
    }

    
  def deleteBucket(bucketName: String): Future[Unit] =
    Future {
      val bucket = File(s"$bucketsDir\\$bucketName")
      bucket.listFiles().foreach(_.delete())
      bucket.delete()
    }
    
  def listBuckets: Future[Buckets] =
    Future {
      Buckets(File(bucketsDir).listFiles().map(bucket => Bucket(name = bucket.getName)))
    }

}
