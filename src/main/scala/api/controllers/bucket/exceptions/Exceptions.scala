package pedro.goncalves
package api.controllers.bucket.exceptions

case class BucketNotExists(name:String) extends Exception
case class BucketExists(name:String) extends Exception
