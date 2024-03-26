package pedro.goncalves
package api.services.objects.structured

import java.sql.{Connection, DriverManager}
import scala.concurrent.Future

case class ParquetFile() {


  private val duckConn: Connection = DriverManager.getConnection("jdbc:duckdb:")
  
  import scala.concurrent.ExecutionContext.Implicits.global
  
  def schema: Future[Array[Map[String, String]]] =
    Future:
      val statement = this.duckConn.createStatement()

      val columnsType = statement.executeQuery(
        """SELECT column_name, data_type
          | FROM information_schema.columns
          | WHERE table_schema='main' AND table_name='main_table';"""
          .stripMargin
      )

      val lazyRows = LazyList.continually(columnsType).takeWhile(_.next())

      lazyRows.map(row =>
        Map(row.getString("column_name") -> row.getString("data_type"))
      ).toArray
}
