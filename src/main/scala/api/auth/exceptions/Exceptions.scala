package pedro.goncalves
package pedro.goncalves.api.auth.exceptions

case class ExpiredToken() extends Exception("Token expired.")

case class NotValidToken() extends Exception("Token not valid.")
