package pedro.goncalves
package database.operations


import database.models.User
import database.models.UserTable.userTable
import scala.concurrent.Future
import slick.jdbc.SQLiteProfile.api.*
import database.Connection.db
import java.security.MessageDigest
import java.util.UUID


object InteractUser:
  
  import scala.concurrent.ExecutionContext.Implicits.global
  
  def createNewUser(user: String, password:String, admin:Boolean,
                    id_profile: Option[String]): Future[Int] =
    db.run(userTable += User(
      id=UUID.randomUUID(),
      username=user,
      password=MessageDigest.getInstance("SHA-256")
        .digest(password.getBytes("UTF-8"))
        .map("%02x".format(_)).mkString,
      admin=admin,
      id_profile=id_profile,
      created_at=Some(java.time.LocalDateTime.now)
    ))

  def existsAdmin: Future[Boolean] =
    val firstAdm = db.run(userTable.filter(_.admin===true).result)
    
    firstAdm.map( adm =>
      adm.headOption match
        case Some(_) => true
        case None => false
    )
    
    
