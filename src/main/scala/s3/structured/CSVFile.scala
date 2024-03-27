package pedro.goncalves
package s3.structured

import s3.metadata

import org.json4s.JInt
import org.json4s.JsonAST.{JDouble, JObject, JString}
import org.json4s.native.JsonMethods.*
import s3.organizer.repository.Repository
import java.io.File
import java.sql.{Connection, DriverManager}
import scala.concurrent.Future
import s3.metadata.Metadata


case class CSVFile(
                    repository:Repository,
                    filePath:String,
                    versioned:Boolean,
                    version:Float=0f
                  ) extends Metadata:

  import scala.concurrent.ExecutionContext.Implicits.global


  //file informations
  private val path = filePath
  val metadataPath: String = if versioned then s"${repository.organizerPath}\\v${version+1}\\$metadataFileName" else s"${repository.organizerPath}\\$metadataFileName"

  //creating table
  implicit val duckConn: Connection = DriverManager.getConnection("jdbc:duckdb:")
  private val statement = duckConn.createStatement()
    statement.execute(
      s"""
         |CREATE TABLE main_table AS
         | SELECT * FROM read_csv('$path');"""
        .stripMargin
    )


  def sniffConfigurations: Future[JObject] =
    Future:
      val statement = duckConn.createStatement()

      val createSniffTable = statement.execute(
        s"""create or replace table
           | sniff_conf as select * from sniff_csv('$path')"""
          .stripMargin
      )

      val sniffResults = statement.executeQuery(
      """SELECT * from 'sniff_conf';""".stripMargin
      )

      val lazyResults = LazyList.continually(sniffResults).takeWhile(_.next())
      val mapMetadata = lazyResults.map(row =>
        Map(
          "dlt_column" -> row.getString("Delimiter"),
          "dlt_line" -> row.getString("NewLineDelimiter"),
          "schema" -> row.getString("Columns"),
          "dt_format" -> row.getString("DateFormat"),
          "tms_format" -> row.getString("TimestampFormat"),
          "pvt_prompt" -> row.getString("Prompt"),
        )
      ).toArray.head
      val jsonValue: JObject = JObject(mapMetadata.map {
        case (key, value) => key -> JString(value)
      }.toSeq *)

      jsonValue
  
  def countRows: Future[JInt] =
    Future:
      val statement = this.duckConn.createStatement()
      val totalLinesStmt = statement.executeQuery(
        """SELECT COUNT(*) AS total FROM 'main_table';""".stripMargin
      )
      totalLinesStmt.next()
      val numRows = totalLinesStmt.getInt("total")
      JInt(numRows)


  def countColumns: Future[JInt] =
    Future:
      val statement = this.duckConn.createStatement()
      val countColumnsStmt = statement.executeQuery(
        """SELECT COUNT(*) AS num_columns
          |FROM information_schema.columns
          | WHERE table_schema='main' AND table_name='main_table';"""
          .stripMargin
      )
      countColumnsStmt.next()
      val numColumns = countColumnsStmt.getInt("num_columns")
      JInt(numColumns)


  def size: JDouble =
    val sizeInBytes = File(path).length()
    val sizeInMB = sizeInBytes / (1024*1024).toDouble
    val formattedSize = f"$sizeInMB%.2f".replace(",",".").toFloat
    JDouble(formattedSize)


  override def _content: Future[JObject] =
    val totalRows: Future[JInt] = this.countRows
    val totalColumns: Future[JInt] = this.countColumns
    val sniffConf: Future[JObject] = this.sniffConfigurations
    val size: JDouble = this.size

    val combinedFuture: Future[JObject] = for {
      rows <- totalRows
      columns <- totalColumns
      sniff <- sniffConf
    } yield {
      val rowsObject = JObject("total_rows" -> rows)
      val columnsObject = JObject("total_columns" -> columns)
      val sizeObject = JObject("size" -> size)

      rowsObject.merge(columnsObject).merge(sizeObject).merge(sniff)
    }

    combinedFuture

  override def _generate: Future[Unit] =
    val metadata: Future[JObject] = this._content

    metadata.flatMap { value =>
      this._create(value)
    }

  override def _read: Future[Map[String, Any]] = ???

  override def _disability: Future[Unit] = ???
    
