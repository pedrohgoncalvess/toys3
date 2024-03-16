package pedro.goncalves
package utils


import java.nio.file.{Files, Paths}
import scala.annotation.tailrec


object configs {

  @tailrec
  def projectPath(currentPath: String = System.getProperty("user.dir")): String = {
    val path = Paths.get(currentPath)
    if (Files.exists(path.resolve("build.sbt"))) {
      path.toString
    } else {
      val parentPath = path.getParent
      if (parentPath != null) {
        projectPath(parentPath.toString)
      } else ""
    }
  }

}
