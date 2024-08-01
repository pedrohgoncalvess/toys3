package pedro.goncalves
package database.migration


import com.typesafe.config.{Config, ConfigFactory}
import org.flywaydb.core.Flyway


object Main:

  private val config: Config = ConfigFactory.load()

  private val dbUrl: String = config.getString("sqlite.db.url")

  val flyway: Flyway = Flyway.configure
    .dataSource(dbUrl, null, null)
    .baselineOnMigrate(true)
    .load()