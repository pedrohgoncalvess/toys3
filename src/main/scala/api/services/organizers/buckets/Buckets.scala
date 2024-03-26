package pedro.goncalves
package api.services.organizers.buckets

import api.json
import api.services.objects.Metadata
import utils.configs.bucketsPath

import org.json4s.JsonAST.JObject
import org.json4s.native.JsonMethods.*
import org.json4s.*

import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Class that represents bucket
  *
  * @param bucketName the name of bucket
  *
  */
case class Buckets (
             bucketName:String
                   ):

  private val metadataBucket = Metadata(bucket=this.bucketName)

  /**
   * A method that checks to see if that bucket exists
   *
   * @return true for exist and false for non exists
   */
  def check: Future[Boolean] =
    Future {
      File(s"$bucketsPath\\$bucketName").exists()
    }


  /**
   * private method used only by the create method that creates the bucket
   * 
   * @return future unit with the result of the operation
   */
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
    metadataBucket.softDelete

  
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
