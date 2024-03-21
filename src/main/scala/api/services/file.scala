package pedro.goncalves
package api.services

import java.io.File
import api.services.buckets.bucketsDir

import java.sql.Connection
import java.sql.DriverManager
import scala.concurrent.Future;

object file extends App {

  import scala.concurrent.ExecutionContext.Implicits.global

  def createRepository(bucketName:String, fileName: String): Unit =
      File(s"$bucketsDir\\$bucketName\\$fileName").mkdir()

  def deleteRepository(bucketName:String, repositoryName:String): Future[Unit] =
    Future {
      val repository = File(s"$bucketsDir\\$bucketName\\$repositoryName")
      repository.listFiles().foreach(_.delete())
      repository.delete()
    }

  Class.forName("org.duckdb.DuckDBDriver")

  val conn:Connection = DriverManager.getConnection("jdbc:duckdb:");

  def loadCsvFile(csvPath: String, duckConn:Connection): Unit =
    val loadFile =
      s"""
        | CREATE TABLE main AS
        | SELECT * FROM read_csv('$csvPath');""".stripMargin

    val statement = duckConn.createStatement()
    statement.execute(loadFile)

    val results = statement.executeQuery("SELECT * FROM main")
    while(results.next()) {
      println(results.getString(1))
    }
    duckConn.close()

  //loadCsvFile("C:\\Users\\Pedro\\Desktop\\WorkSpace\\Projetos\\toys3\\toys3\\buckets\\balde\\example.csv", conn)
}
