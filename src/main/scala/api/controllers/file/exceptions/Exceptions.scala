package pedro.goncalves
package api.exceptions.file

class InconsistentParameters extends Exception

case class InconsistentRepositoryVersion(version:Float) extends Exception
