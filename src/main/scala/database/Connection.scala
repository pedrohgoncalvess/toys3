package pedro.goncalves
package database


import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import slick.jdbc.PostgresProfile.api._


object Connection {
  val config = ConfigFactory.load
  
  lazy val url = config.getString("sqlite.db.url")
  lazy val driver = config.getString("sqlite.db.driver")
  lazy val storageDir = config.getString("sqlite.dir")
  
  val db = {
    val config = new HikariConfig
    config.setDriverClassName(driver)
    config.setJdbcUrl(url)
    config.setConnectionTestQuery("SELECT 1")
    config.setIdleTimeout(10000)
    config.setMaximumPoolSize(20)

    val ds = new HikariDataSource(config)
    Database.forDataSource(ds, Option(20))
  }
}
