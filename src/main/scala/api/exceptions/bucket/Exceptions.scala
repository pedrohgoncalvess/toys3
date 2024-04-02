package pedro.goncalves
package api.exceptions.bucket

case class BucketNotExists(name:String) extends Exception
case class BucketExists(name:String) extends Exception
case class UUIdBucketNotExists(id:String) extends Exception
