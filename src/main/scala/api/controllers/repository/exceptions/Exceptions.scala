package pedro.goncalves
package api.exceptions.repository

case class RepositoryExists(name:String) extends Exception

case class RepositoryNotExists(name:String) extends Exception

//TODO: Create common exceptions file to centralize not dedicate exceptions
case class DelTypeNotExists(_type:String) extends Exception