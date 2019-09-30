package de.eaglefamily.oregenerator;

import org.bukkit.plugin.java.JavaPlugin;

public class OreGeneratorPlugin extends JavaPlugin {

  @Override
  public void onEnable() {
    final long initStartTime = System.currentTimeMillis();
    getLogger().info("Initializing OreGenerator");


    final long initDuration = System.currentTimeMillis() - initStartTime;
    getLogger()
        .info(String.format("Successfully initialized OreGenerator. (took %sms)", initDuration));
  }

  @Override
  public void onDisable() {
  }
}
