package pedro.goncalves
package s3.organizer

import scala.concurrent.Future

trait Organizer {
  
  val organizerPath:String

  /**
   * A method that materialize the organizer
   *
   * @return result of creation operation
   */
  def create: Future[Unit]
  
  /**
   * A method that checks to see if that organizer exists
   *
   * @return true for exist and false for non exists
   */
  def check: Boolean
  
  /**
   * A method that exclude all files and dirs inside of organizer
   *
   * @return result of delete operation
   */
  def exclude: Future[Unit]

}
