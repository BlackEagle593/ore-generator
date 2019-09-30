package de.eaglefamily.oregenerator.listener;

import de.eaglefamily.oregenerator.generator.OreGenerator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

  private final OreGenerator oreGenerator;

  /**
   * Creates an instance of block break listener.
   *
   * @param oreGenerator the ore generator
   */
  public BlockBreakListener(final OreGenerator oreGenerator) {
    this.oreGenerator = oreGenerator;
  }

  /**
   * Handles block break events.
   *
   * @param event the event
   */
  @EventHandler
  public void onBlockBreak(final BlockBreakEvent event) {
    oreGenerator.addBlock(event.getBlock(), event.getPlayer());
  }
}
