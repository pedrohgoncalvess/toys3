package pedro.goncalves
package pedro.goncalves.api.bucket.exceptions

case class BucketNotExists(name:String) extends Exception
case class BucketExists(name:String) extends Exception
