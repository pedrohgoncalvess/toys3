package pedro.goncalves
package s3.utils


import java.util.UUID
import scala.annotation.tailrec


@tailrec
def generateNewUUID(uuids: Array[String]): String =
  val uuid = UUID.randomUUID().toString
  if (!uuids.contains(uuid))
    uuid
  else
    generateNewUUID(uuids)
