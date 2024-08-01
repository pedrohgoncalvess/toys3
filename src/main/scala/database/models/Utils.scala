package pedro.goncalves
package database.models


import java.util.UUID


object Utils:

  import slick.jdbc.SQLiteProfile.api._

  implicit val uuidColumnType: BaseColumnType[UUID] = MappedColumnType.base[UUID, String](
    _.toString,
    UUID.fromString
  )

  implicit val uuidArrayColumnType: BaseColumnType[Option[Array[UUID]]] = MappedColumnType.base[Option[Array[UUID]], String](
    _.map(_.map(_.toString).mkString(",")).getOrElse(""),
    str => if (str.isEmpty) None else Some(str.split(",").map(UUID.fromString))
  )
