package pedro.goncalves
package database.models


import java.time.LocalDateTime
import java.util.UUID


case class Profile(
                 id: UUID,
                 buckets: Option[Array[UUID]],
                 repositories: Option[Array[UUID]],
                 status:Boolean,
                 created_at: Option[LocalDateTime]
               )

object Profile {
  def tupled: ((UUID, Option[Array[UUID]], Option[Array[UUID]], Boolean, Option[LocalDateTime])) => Profile =
    (Profile.apply _).tupled
}

object ProfileTable {

  import slick.jdbc.SQLiteProfile.api._

  implicit val uuidColumnType:BaseColumnType[UUID] = MappedColumnType.base[UUID, String](
    _.toString,
    UUID.fromString
  )

  implicit val uuidArrayColumnType: BaseColumnType[Option[Array[UUID]]] = MappedColumnType.base[Option[Array[UUID]], String](
    _.map(_.map(_.toString).mkString(",")).getOrElse(""),
    str => if (str.isEmpty) None else Some(str.split(",").map(UUID.fromString))
  )

  class ProfileTable(tag: Tag) extends Table[Profile](tag, "profile") {
    def id = column[UUID]("id", O.PrimaryKey)
    def buckets = column[Option[Array[UUID]]]("buckets")
    def repositories = column[Option[Array[UUID]]]("repositories")
    def status = column[Boolean]("status", O.Default(true))
    def created_at = column[Option[LocalDateTime]]("created_at")


    override def * = (
      id,
      buckets,
      repositories,
      status,
      created_at
    ) <> (Profile.tupled, Profile.unapply)
  }

  lazy val profileTable = TableQuery[ProfileTable]
}