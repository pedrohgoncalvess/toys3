package pedro.goncalves
package database


import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import slick.jdbc.PostgresProfile.api.*

import java.io.File
import scala.concurrent.Await
import scala.concurrent.duration.*


object Connection:
  private val config = ConfigFactory.load
  
  private lazy val url = config.getString("sqlite.db.url")
  private lazy val driver = config.getString("sqlite.db.driver")
  private lazy val dbPath = config.getString("sqlite.dir")
  
  lazy val db = {
    val config = new HikariConfig
    config.setDriverClassName(driver)
    config.setJdbcUrl(url)
    config.setConnectionTestQuery("SELECT 1")
    config.setIdleTimeout(10000)
    config.setMaximumPoolSize(20)
  
    val ds = new HikariDataSource(config)
    Database.forDataSource(ds, Option(20))
  }
  
  def initializeDatabase: Unit =
    val dbFile = new File(dbPath)
    if (!dbFile.exists()) {
      println(".storage dir not found. Creating database file.")
      dbFile.mkdirs()
  
      val setupAction = sql"PRAGMA user_version".as[Int]
  
      val setupFuture = db.run(setupAction)
      Await.result(setupFuture, 10.seconds)
      println("Database file created has successfully.")
    } else {
      println("Database file already exists.")
    }

