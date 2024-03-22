package pedro.goncalves
package api.services

import utils.configs.bucketsPath
import api.json
import org.json4s.{JInt, JString}
import org.json4s.JsonAST.JObject
import org.json4s.*
import org.json4s.native.JsonMethods.*
import scala.concurrent.Future
import java.io.File
import api.services.objects.Metadata

import scala.concurrent.ExecutionContext.Implicits.global

case class Buckets(
             bucketName:String
             ):

  private val metadataBucket = new Metadata(bucket=this.bucketName)
  
  
  def check: Future[Boolean] =
    Future {
      File(s"$bucketsPath\\$bucketName").exists()
    }

  private def createMetadata: Future[Unit] =
    val json = JObject(
      "created_at" -> JString(java.time.LocalDateTime.now().toString),
      "created_by" -> JString("admin"),
      "objects" -> JInt(0),
      "active" -> JBool(true)
    )
    metadataBucket.create(json)
    
  def create: Future[Unit] =
    Future {
      File(s"$bucketsPath\\${this.bucketName}").mkdir()
      this.createMetadata
    }

  def softDelete: Future[Unit] =
    Future {
      metadataBucket.softDelete
    }
  
  def delete: Future[Unit] =
    Future {
      val bucket = File(s"$bucketsPath\\${this.bucketName}")
      bucket.listFiles().foreach(_.delete)
      bucket.delete()
    }
    
def listBuckets: Future[json.Buckets] =
  Future {
    json.Buckets(File(bucketsPath).listFiles.map(bucket => json.Bucket(name = bucket.getName)))
  }
