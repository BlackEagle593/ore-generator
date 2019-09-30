package de.eaglefamily.oregenerator.database;

import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;
import org.bukkit.Bukkit;
import org.flywaydb.core.Flyway;

class DatabaseMigrator {

  /**
   * Migrates the database with flyway.
   *
   * @param dataSource the data source
   * @param schema     the schema
   */
  void migrate(final DataSource dataSource, final String schema) {
    final Flyway flyway = Flyway.configure(getClass().getClassLoader())
        .encoding(StandardCharsets.UTF_8)
        .dataSource(dataSource)
        .schemas(schema)
        .load();

    Bukkit.getLogger().info("Checking if migration is needed.");
    if (flyway.info().pending().length > 0) {
      Bukkit.getLogger().info("Database migration needed. Starting migration.");
      flyway.migrate();
    } else {
      Bukkit.getLogger().info("Database is up-to-date.");
    }
  }
}
