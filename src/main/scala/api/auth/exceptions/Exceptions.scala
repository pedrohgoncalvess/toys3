package pedro.goncalves
package api.auth.exceptions

case class ExpiredToken() extends Exception("Token expired.")

case class NotValidToken() extends Exception("Token not valid.")
