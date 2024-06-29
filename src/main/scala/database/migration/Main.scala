package pedro.goncalves
package database.migration


import com.typesafe.config.{Config, ConfigFactory}
import org.flywaydb.core.Flyway


object Migration {

  private val config: Config = ConfigFactory.load()

  private val dbUrl: String = config.getString("sqlite.db.url")
  private val dbDriver: String = config.getString("sqlite.db.driver")

  val flyway: Flyway = Flyway.configure
    .dataSource(dbUrl, null, null)
    .load()
}