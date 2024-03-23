package pedro.goncalves
package api.services.objects

import org.json4s.JsonAST.JObject

import java.sql.{Connection, DriverManager, ResultSet}
import scala.concurrent.Future

case class RelationalFile(
          repository:Repository,
          fileName:String,
          extension:String,
          separator:String=null
          ):

  import scala.concurrent.ExecutionContext.Implicits.global

  val filePath = s"${this.repository.repositoryPath}\\${this.fileName}"

  private val duckConn: Connection = DriverManager.getConnection("jdbc:duckdb:")

  private val statement = duckConn.createStatement()
    statement.execute(
      s"""
         |CREATE TABLE csv_data AS
         | SELECT * FROM read_csv('$filePath');"""
        .stripMargin
    )

  def schema: Future[Array[Map[String,String]]] =
    Future:
      val statement = this.duckConn.createStatement()

      val columnsType = statement.executeQuery(
        """SELECT column_name, data_type
          | FROM information_schema.columns
          | WHERE table_schema='main' AND table_name='csv_data';"""
          .stripMargin
      )

      val lazyRows = LazyList.continually(columnsType).takeWhile(_.next())

      lazyRows.map(row =>
        Map(row.getString("column_name") -> row.getString("data_type"))
      ).toArray


  def countRows: Future[Map[String,Int]] =
    Future:
      val statement = this.duckConn.createStatement()
      val totalLinesStmt = statement.executeQuery(
        """SELECT COUNT(*) AS total FROM 'csv_data';""".stripMargin
      )
      totalLinesStmt.next()
      val numRows = totalLinesStmt.getInt("total")
      Map("num_rows" -> numRows)


  def countColumn: Future[Map[String,Int]] =
    Future:
      val statement = this.duckConn.createStatement()
      val countColumnsStmt = statement.executeQuery(
        """SELECT COUNT(*) AS num_columns
          |FROM information_schema.columns
          | WHERE table_schema='main' AND table_name='csv_data';"""
          .stripMargin
      )
      countColumnsStmt.next()
      val numColumns = countColumnsStmt.getInt("num_columns")
      Map("num_columns" -> numColumns)
      
      
object legal extends App {
  
}
