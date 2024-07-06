package pedro.goncalves
package api.file.exceptions

class InconsistentParameters extends Exception

case class InconsistentRepositoryVersion(version:Float) extends Exception
