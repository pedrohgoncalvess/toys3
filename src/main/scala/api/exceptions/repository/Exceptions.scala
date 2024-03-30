package pedro.goncalves
package api.exceptions.repository

case class RepositoryExists(repositoryName:String) extends Exception

case class RepositoryNotExists(repositoryName:String) extends Exception
