package de.eaglefamily.oregenerator.database;

import io.reactivex.rxjava3.core.Single;
import java.io.Closeable;
import java.io.File;
import java.util.UUID;

public interface Database extends Closeable {

  /**
   * Initializes the database.
   *
   * @param dataFolder the data folder
   * @param pluginName the plugin name
   */
  void initialize(File dataFolder, String pluginName);

  /**
   * Gets level of a player.
   *
   * @param uuid the uuid of the player
   * @return the single with the level
   */
  Single<Integer> getLevel(UUID uuid);

  /**
   * Sets level of a player.
   *
   * @param uuid  the uuid of the player
   * @param level the level
   * @return ths single with the new level
   */
  Single<Integer> setLevel(UUID uuid, int level);

  /**
   * Increments level of a player.
   *
   * @param uuid the uuid of the player
   * @return the single with the new level
   */
  Single<Integer> incrementLevel(UUID uuid);
}
