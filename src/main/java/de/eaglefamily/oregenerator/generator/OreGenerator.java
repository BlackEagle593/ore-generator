package de.eaglefamily.oregenerator.generator;

import com.google.common.collect.Sets;
import de.eaglefamily.oregenerator.database.Database;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class OreGenerator {

  private final String generatorPlayerMeta = "generator_player";
  private final String generatorTimestampMeta = "generator_timestamp";

  private final Plugin plugin;
  private final Database database;
  private final GeneratorProbability generatorProbability;
  private final Scheduler bukkitRxScheduler;
  private final Set<Block> blocks = Sets.newHashSet();
  private final int timeToReset = 2000;
  private final long repeatDelay = 10000;

  /**
   * Creates an instance of ore generator.
   *
   * @param plugin               the plugin
   * @param database             the database
   * @param generatorProbability the generator probability
   * @param bukkitRxScheduler    the bukkit rx scheduler
   */
  public OreGenerator(final Plugin plugin, final Database database,
                      final GeneratorProbability generatorProbability,
                      final Scheduler bukkitRxScheduler) {
    this.plugin = plugin;
    this.database = database;
    this.generatorProbability = generatorProbability;
    this.bukkitRxScheduler = bukkitRxScheduler;
    removeTimer(bukkitRxScheduler);
  }

  /**
   * Generates a random ore.
   *
   * @param block the block
   */
  public void generate(final Block block) {
    if (!block.hasMetadata(generatorPlayerMeta) || !block.hasMetadata(generatorTimestampMeta)) {
      return;
    }

    final UUID blockPlayerUuid =
        UUID.fromString(block.getMetadata(generatorPlayerMeta).get(0).asString());
    final long blockTimestamp = block.getMetadata(generatorTimestampMeta).get(0).asLong();

    blocks.remove(block);
    block.removeMetadata(generatorPlayerMeta, plugin);
    block.removeMetadata(generatorTimestampMeta, plugin);

    if (blockTimestamp + timeToReset < System.currentTimeMillis()) {
      return;
    }

    database.getLevel(blockPlayerUuid).observeOn(bukkitRxScheduler).subscribe(level -> {
      final Material material = generatorProbability.getRandomOre(level);
      if (material == null) {
        return;
      }

      block.setType(material);
    });
  }

  /**
   * Adds a block to a player for the ore generator to indicate the level of the generator when
   * generating a random ore.
   *
   * @param block  the block
   * @param player the player
   */
  public void addBlock(final Block block, final Player player) {
    block.setMetadata(generatorPlayerMeta, new FixedMetadataValue(plugin, player.getUniqueId()));
    block.setMetadata(generatorTimestampMeta,
        new FixedMetadataValue(plugin, System.currentTimeMillis()));
    blocks.add(block);
  }

  private void removeTimer(final Scheduler bukkitRxScheduler) {
    Observable.timer(repeatDelay, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.newThread())
        .observeOn(bukkitRxScheduler)
        .repeat()
        .subscribe(ignored ->
            blocks.removeIf(block -> {
              final long blockTimestamp = block.getMetadata(generatorTimestampMeta).get(0).asLong();
              if (blockTimestamp + timeToReset > System.currentTimeMillis()) {
                return false;
              }

              block.removeMetadata(generatorPlayerMeta, plugin);
              block.removeMetadata(generatorTimestampMeta, plugin);
              return true;
            }));
  }
}
