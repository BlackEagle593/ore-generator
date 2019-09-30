package de.eaglefamily.oregenerator.database;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.RowCountQuery;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;

public class PostgresDatabase implements Database {

  private Table<Record> levelTable;
  private Field<UUID> uuidField = field("uuid", UUID.class);
  private Field<Integer> levelField = field("generator_level", Integer.class);

  private HikariDataSource dataSource;
  private DSLContext dslContext;

  @Override
  public void initialize(final File dataFolder, final String pluginName) {
    System.getProperties().setProperty("org.jooq.no-logo", "true");
    Properties properties = loadDatabaseFile(dataFolder);

    dataSource = new HikariDataSource(new HikariConfig(properties));
    dslContext = DSL.using(dataSource, SQLDialect.POSTGRES);

    final String schema = pluginName.toLowerCase(Locale.ROOT);

    final DatabaseMigrator migrator = new DatabaseMigrator();
    migrator.migrate(dataSource, schema);

    levelTable = table(schema + ".player_generator_levels");
  }

  @Override
  public Single<Integer> getLevel(final UUID uuid) {
    return rxQuery(dslContext.selectFrom(levelTable).where(uuidField.eq(uuid)))
        .firstElement()
        .map(record -> record.get(levelField))
        .switchIfEmpty(Single.just(0));
  }

  @Override
  public Single<Integer> setLevel(final UUID uuid, final int level) {
    return rxQuery(dslContext.insertInto(levelTable)
        .columns(uuidField, levelField)
        .values(uuid, level)
        .onConflict(uuidField)
        .doUpdate()
        .set(levelField, level))
        .map(ignored -> level);
  }

  @Override
  public Single<Integer> incrementLevel(final UUID uuid) {
    return getLevel(uuid).flatMap(level -> setLevel(uuid, level + 1));
  }

  @Override
  public void close() {
    if (dataSource != null) {
      dataSource.close();
    }

    if (dslContext != null) {
      dslContext.close();
    }
  }

  private Properties loadDatabaseFile(final File dataFolder) {
    final String databasePropName = "database.properties";
    final File databasePropFile = new File(dataFolder, databasePropName);

    if (!dataFolder.exists() && !dataFolder.mkdirs()) {
      Bukkit.getLogger().log(Level.SEVERE, String.format("Could not create directory %s.",
          dataFolder.getPath()));
      return null;
    }

    // Copy database properties from resources to data folder
    if (!databasePropFile.exists()) {
      Bukkit.getLogger().info(String.format("%s not found. Creating new file.", databasePropName));

      try (InputStream input = getClass().getResourceAsStream("/" + databasePropName)) {
        Files.copy(input, databasePropFile.toPath());
        Bukkit.getLogger()
            .info(String.format("%s successfully created. Please setup the database data",
                databasePropName));
      } catch (final IOException e) {
        Bukkit.getLogger()
            .log(Level.SEVERE, String.format("Could not copy %s from resources", databasePropName),
                e);
      }
    }

    final Properties properties = new Properties();

    try {
      properties.load(new FileInputStream(databasePropFile));
    } catch (final IOException e) {
      Bukkit.getLogger().log(Level.SEVERE, "Could not load properties file", e);
    }

    return properties;
  }

  private Single<Integer> rxQuery(final RowCountQuery query) {
    return Single.fromPublisher(query).subscribeOn(Schedulers.io());
  }

  private <T extends Record> Flowable<T> rxQuery(final ResultQuery<T> resultQuery) {
    return Flowable.fromPublisher(resultQuery)
        .onBackpressureBuffer()
        .subscribeOn(Schedulers.io());
  }
}
