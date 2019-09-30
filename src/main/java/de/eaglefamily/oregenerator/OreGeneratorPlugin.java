package de.eaglefamily.oregenerator;

import de.eaglefamily.oregenerator.command.OreGeneratorCommand;
import de.eaglefamily.oregenerator.database.Database;
import de.eaglefamily.oregenerator.database.PostgresDatabase;
import de.eaglefamily.oregenerator.generator.GeneratorPrice;
import de.eaglefamily.oregenerator.generator.GeneratorProbability;
import de.eaglefamily.oregenerator.generator.OreGenerator;
import de.eaglefamily.oregenerator.listener.BlockBreakListener;
import de.eaglefamily.oregenerator.listener.BlockFromToListener;
import de.eaglefamily.oregenerator.util.BukkitRxWorker;
import de.eaglefamily.oregenerator.util.ResourceMessage;
import java.io.IOException;
import java.util.Objects;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class OreGeneratorPlugin extends JavaPlugin {

  private Database database;

  @Override
  public void onEnable() {
    final long initStartTime = System.currentTimeMillis();
    getLogger().info("Initializing OreGenerator");

    // Initialize config
    getConfig().options().copyDefaults(true);
    saveConfig();

    // Initialize database
    database = new PostgresDatabase();
    database.initialize(getDataFolder(), getName());

    final BukkitRxWorker bukkitRxWorker = new BukkitRxWorker(this);
    final ResourceMessage resourceMessage = new ResourceMessage(this);
    final GeneratorProbability generatorProbability = new GeneratorProbability(getConfig());
    final GeneratorPrice generatorPrice = new GeneratorPrice(getConfig());
    final OreGenerator oreGenerator = new OreGenerator(this, database, generatorProbability,
        bukkitRxWorker.getScheduler());

    // Register listener
    final PluginManager pluginManager = getServer().getPluginManager();
    pluginManager.registerEvents(new BlockBreakListener(oreGenerator), this);
    pluginManager.registerEvents(new BlockFromToListener(this, oreGenerator), this);

    // Register commands
    Objects.requireNonNull(getCommand("oregenerator"))
        .setExecutor(new OreGeneratorCommand(this, database, bukkitRxWorker.getScheduler(),
            resourceMessage, generatorPrice));

    final long initDuration = System.currentTimeMillis() - initStartTime;
    getLogger()
        .info(String.format("Successfully initialized OreGenerator. (took %sms)", initDuration));
  }

  @Override
  public void onDisable() {
    if (database != null) {
      try {
        database.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
