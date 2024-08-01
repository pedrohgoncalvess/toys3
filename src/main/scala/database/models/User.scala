package pedro.goncalves
package database.models


import java.time.LocalDateTime
import java.util.UUID


case class User(
                 id: UUID,
                 username: String,
                 password: String,
                 id_profile: Option[String],
                 admin: Boolean,
                 created_at: Option[LocalDateTime]
               )


object User:
  def tupled: ((UUID, String, String, Option[String], Boolean, Option[LocalDateTime])) => User =
    User.apply.tupled


object UserTable:

  import slick.jdbc.SQLiteProfile.api._
  import database.models.Utils._

  class UserTable(tag: Tag) extends Table[User](tag, "user") {
    def id = column[UUID]("id", O.PrimaryKey)
    def username = column[String]("username")
    def password = column[String]("password")
    def id_profile = column[Option[String]]("id_profile")
    def admin = column[Boolean]("admin")
    def created_at = column[Option[LocalDateTime]]("created_at")
    
    override def * = (
      id,
      username,
      password,
      id_profile,
      admin,
      created_at
    ) <> (User.tupled, User.unapply)
  }

  lazy val userTable = TableQuery[UserTable]