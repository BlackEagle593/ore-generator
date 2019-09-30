package de.eaglefamily.oregenerator.listener;

import de.eaglefamily.oregenerator.generator.OreGenerator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.plugin.Plugin;

public class BlockFromToListener implements Listener {

  private final Plugin plugin;
  private final OreGenerator oreGenerator;

  /**
   * Creates an instance of block from to listener.
   *
   * @param plugin       the plugin
   * @param oreGenerator the ore generator
   */
  public BlockFromToListener(final Plugin plugin, final OreGenerator oreGenerator) {
    this.plugin = plugin;
    this.oreGenerator = oreGenerator;
  }

  /**
   * Handles block from to event.
   *
   * @param event the block from to event
   */
  @EventHandler
  public void onBlockFromTo(final BlockFromToEvent event) {
    final Block toBlock = event.getToBlock();
    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
      if (toBlock.getType() != Material.COBBLESTONE) {
        return;
      }

      oreGenerator.generate(event.getToBlock());
    });
  }
}
