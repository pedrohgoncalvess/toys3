package pedro.goncalves
package api.user

import java.security.MessageDigest
import java.util.Base64


object Service:

  def calculateHash(input: String): String =
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(input.getBytes("UTF-8"))
    Base64.getEncoder.encodeToString(hashBytes)
