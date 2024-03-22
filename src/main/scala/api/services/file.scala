package pedro.goncalves
package api.services


import java.sql.{Connection, DriverManager, ResultSet}

object file extends App:

  val conn: Connection = DriverManager.getConnection("jdbc:duckdb:")

  def loadCsvFile(csvPath: String, duckConn: Connection): Connection =
    val loadFile =
      s"""
         | CREATE TABLE csv_data AS
         | SELECT * FROM read_csv('$csvPath');""".stripMargin

    val statement = duckConn.createStatement()
    statement.execute(loadFile)

    //Total columns
    val countColumnsStmt = statement.executeQuery(
      """SELECT COUNT(*) AS num_columns
        |FROM information_schema.columns
        | WHERE table_schema='main' AND table_name='csv_data';"""
        .stripMargin
    )
    countColumnsStmt.next()
    val numColumns = countColumnsStmt.getInt("num_columns")

    //Total rows
    val totalLinesStmt = statement.executeQuery(
    """SELECT COUNT(*) AS total FROM 'csv_data';"""
      .stripMargin
    )
    totalLinesStmt.next()
    val numRows = totalLinesStmt.getInt("total")


    //Schema data
    val columnsType = statement.executeQuery(
    """SELECT column_name, data_type
        | FROM information_schema.columns
        | WHERE table_schema='main' AND table_name='csv_data';"""
      .stripMargin
    )

    val lazyRows = LazyList.continually(columnsType).takeWhile(_.next())
    val schemaInformation = lazyRows.map(row =>
    Map(row.getString("column_name") -> row.getString("data_type"))
    ).toArray

    duckConn

  def teste(duckConn:Connection): Unit =
    val statement = duckConn.createStatement()
    val countColumnsStmt = statement.executeQuery(
      """SELECT COUNT(*) AS num_columns
        |FROM information_schema.columns
        | WHERE table_schema='main' AND table_name='csv_data';"""
        .stripMargin
    )
    countColumnsStmt.next()
    val numColumns = countColumnsStmt.getInt("num_columns")
    println(numColumns)

  loadCsvFile("C:\\Users\\Pedro\\Desktop\\WorkSpace\\Projetos\\toys3\\toys3\\buckets\\balde\\example\\example.csv", conn)
  teste(conn)


