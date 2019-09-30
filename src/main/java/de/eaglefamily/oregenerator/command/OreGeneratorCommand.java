package de.eaglefamily.oregenerator.command;

import de.eaglefamily.oregenerator.database.Database;
import de.eaglefamily.oregenerator.generator.GeneratorGui;
import de.eaglefamily.oregenerator.generator.GeneratorPrice;
import de.eaglefamily.oregenerator.util.ResourceMessage;
import io.reactivex.rxjava3.core.Scheduler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class OreGeneratorCommand implements CommandExecutor {

  private final Plugin plugin;
  private final Database database;
  private final Scheduler bukkitRxScheduler;
  private final ResourceMessage resourceMessage;
  private final GeneratorPrice generatorPrice;

  /**
   * Creates an instance of ore generator command.
   *
   * @param plugin            the plugin
   * @param database          the databse
   * @param bukkitRxScheduler the bukkit rx scheduler
   * @param resourceMessage   the resource message
   * @param generatorPrice    the generator price
   */
  public OreGeneratorCommand(final Plugin plugin, final Database database,
                             final Scheduler bukkitRxScheduler,
                             final ResourceMessage resourceMessage,
                             final GeneratorPrice generatorPrice) {
    this.plugin = plugin;
    this.database = database;
    this.bukkitRxScheduler = bukkitRxScheduler;
    this.resourceMessage = resourceMessage;
    this.generatorPrice = generatorPrice;
  }

  @Override
  public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command,
                           @NotNull final String label, @NotNull final String[] args) {
    if (!(sender instanceof Player)) {
      resourceMessage.sendMessage(sender, "command.oregenerator.only_for_players");
      return true;
    }

    final Player player = (Player) sender;
    GeneratorGui generatorGui = new GeneratorGui(plugin, database, bukkitRxScheduler, player,
        resourceMessage, generatorPrice);
    generatorGui.openInventory();

    return true;
  }
}
