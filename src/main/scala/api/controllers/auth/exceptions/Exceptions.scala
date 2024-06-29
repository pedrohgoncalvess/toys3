package pedro.goncalves
package api.controllers.auth.exceptions

case class ExpiredToken() extends Exception("Token expired.")

case class NotValidToken() extends Exception("Token not valid.")
