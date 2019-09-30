package de.eaglefamily.oregenerator.generator;

import de.eaglefamily.oregenerator.database.Database;
import de.eaglefamily.oregenerator.util.ResourceMessage;
import io.reactivex.rxjava3.core.Scheduler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class GeneratorGui implements Listener {

  private final int upgradeSlot = 13;

  private final Plugin plugin;
  private final Database database;
  private final Scheduler bukkitRxScheduler;
  private final Player player;
  private final ResourceMessage resourceMessage;
  private final GeneratorPrice generatorPrice;

  private InventoryView generatorView;
  private int currentLevel;

  /**
   * Creates an instance of generator gui.
   *
   * @param plugin            the plugin
   * @param database          the database
   * @param bukkitRxScheduler the bukkit rx scheduler
   * @param player            the player
   * @param resourceMessage   the resource message
   * @param generatorPrice    the generator price
   */
  public GeneratorGui(final Plugin plugin, final Database database,
                      final Scheduler bukkitRxScheduler, final Player player,
                      final ResourceMessage resourceMessage,
                      final GeneratorPrice generatorPrice) {
    this.plugin = plugin;
    this.database = database;
    this.bukkitRxScheduler = bukkitRxScheduler;
    this.player = player;
    this.resourceMessage = resourceMessage;
    this.generatorPrice = generatorPrice;
  }


  /**
   * Handles inventory close event.
   *
   * @param event the event
   */
  @EventHandler
  public void onInventoryClose(final InventoryCloseEvent event) {
    if (event.getPlayer() != player) {
      return;
    }

    HandlerList.unregisterAll(this);
  }

  /**
   * Handles plugin disable event.
   *
   * @param event the event
   */
  @EventHandler
  public void onPluginDisable(final PluginDisableEvent event) {
    if (event.getPlugin() != plugin) {
      return;
    }

    player.closeInventory();
  }

  /**
   * Handles inventory click event.
   *
   * @param event the event
   */
  @EventHandler
  public void onInventoryClick(final InventoryClickEvent event) {
    if (event.getWhoClicked() != player) {
      return;
    }

    if (generatorView == null || player.getOpenInventory() != generatorView) {
      return;
    }

    event.setCancelled(true);

    if (event.getClickedInventory() != generatorView.getTopInventory()) {
      return;
    }
    if (event.getSlot() != upgradeSlot) {
      return;
    }

    final ItemStack currentItem = event.getCurrentItem();
    if (currentItem == null) {
      return;
    }

    upgradeGenerator();
  }

  /**
   * Opens the gui inventory of the generator.
   */
  public void openInventory() {
    database.getLevel(player.getUniqueId()).observeOn(bukkitRxScheduler)
        .subscribe(this::openInventory);
  }

  private void openInventory(final int level) {
    currentLevel = level;
    generatorView = player.openInventory(createInventory(level));
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  private Inventory createInventory(final int level) {
    Inventory oreGeneratorGui = Bukkit.createInventory(null, 9 * 3,
        resourceMessage.getString(player, "gui.generator.title", level));

    final ItemStack filler = fillerItem();
    for (int i = 0; i < oreGeneratorGui.getSize(); i++) {
      oreGeneratorGui.setItem(i, filler);
    }

    final ItemStack upgrade = upgradeItem(level);
    oreGeneratorGui.setItem(upgradeSlot, upgrade);

    return oreGeneratorGui;
  }

  private ItemStack fillerItem() {
    final ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    final ItemMeta meta = filler.getItemMeta();
    meta.setDisplayName("§0");
    filler.setItemMeta(meta);
    return filler;
  }

  private ItemStack upgradeItem(final int level) {
    final int nextLevel = level + 1;
    final ItemStack upgrade = new ItemStack(Material.BOOK);
    final ItemMeta meta = upgrade.getItemMeta();

    // Create lore
    final List<String> lore = new ArrayList<>();
    final List<ItemStack> prices = generatorPrice.getPrice(nextLevel);
    if (!prices.isEmpty()) {
      lore.add(resourceMessage.getString(player, "gui.oregenerator.upgrade.lore.title"));
      for (ItemStack price : prices) {
        final String i18nMaterial = resourceMessage.getString(player,
            "material." + price.getType().toString().toLowerCase());
        lore.add(resourceMessage.getString(player, "gui.oregenerator.upgrade.lore.price",
            i18nMaterial, price.getAmount()));
      }
      meta.setLore(lore);

      meta.setDisplayName(
          resourceMessage.getString(player, "gui.generator.upgrade.title", nextLevel));
    } else {
      meta.setDisplayName(
          resourceMessage.getString(player, "gui.generator.upgrade.max_reached", level));
    }


    upgrade.setItemMeta(meta);
    return upgrade;
  }

  private void upgradeGenerator() {
    final List<ItemStack> prices = generatorPrice.getPrice(currentLevel + 1);
    if (!canPlayerUpgrade(prices)) {
      return;
    }

    if (!removePriceItems(prices)) {
      return;
    }

    database.incrementLevel(player.getUniqueId()).observeOn(bukkitRxScheduler)
        .subscribe(this::openInventory, e -> plugin.getLogger().log(Level.SEVERE,
            String.format("An error occurred while incrementing level of %s (%s). Prices: %s",
                player.getName(), player.getUniqueId(), Arrays.toString(prices.toArray())), e));
  }

  /**
   * Checks the prices if the player can upgrade the generator.
   *
   * @param prices the prices for the next generator level
   * @return if the player is allowed to upgrade
   */
  private boolean canPlayerUpgrade(final List<ItemStack> prices) {
    // prices is empty when max level reached
    if (prices.isEmpty()) {
      resourceMessage.sendMessage(player, "generator.upgrade.max_reached");
      return false;
    }

    // check if player has required items in his inventory
    for (ItemStack price : prices) {
      if (!player.getInventory().containsAtLeast(price, price.getAmount())) {
        resourceMessage.sendMessage(player, "generator.upgrade.items_missing");
        return false;
      }
    }

    return true;
  }

  /**
   * Removes the price items from the inventory of the player.
   *
   * @param prices the prices for the next generator level
   * @return true if all items were removed successfully
   */
  private boolean removePriceItems(final List<ItemStack> prices) {
    HashMap<Integer, ItemStack> notRemovedItems =
        player.getInventory().removeItem(prices.toArray(new ItemStack[0]));

    if (!notRemovedItems.isEmpty()) {
      // Normally this error will never occur when player can upgrade
      plugin.getLogger().log(Level.SEVERE,
          String.format("Not all items could be removed. Prices: %s; Not removed Items: %s",
              Arrays.toString(prices.toArray()),
              Arrays.toString(notRemovedItems.entrySet().toArray())));
      return false;
    }
    return true;
  }
}
