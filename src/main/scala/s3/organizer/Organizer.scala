package pedro.goncalves
package s3.organizer

import scala.concurrent.Future

trait Organizer {
  
  val organizerPath:String

  def create: Future[Unit]
  def check: Boolean
  def exclude: Future[Unit]

}
