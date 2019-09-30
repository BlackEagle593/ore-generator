package de.eaglefamily.oregenerator.generator;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class GeneratorPrice {

  private final Configuration config;

  /**
   * Creates an instance of generator pirce.
   *
   * @param config the config
   */
  public GeneratorPrice(final Configuration config) {
    this.config = config;
  }

  /**
   * Gets the generator upgrade prices.
   *
   * @param level the level of the generator to upgrade to
   * @return the list of item stacks needed to upgrade the generator
   */
  List<ItemStack> getPrice(final int level) {
    List<ItemStack> prices = Lists.newArrayList();

    final String pricesKey = "prices";
    final ConfigurationSection levelPriceConfig =
        config.getConfigurationSection(pricesKey + "." + level);
    if (levelPriceConfig == null) {
      return prices;
    }

    for (final String key : levelPriceConfig.getKeys(false)) {
      final Optional<Material> optionalMaterial = Enums.getIfPresent(Material.class, key);
      if (!optionalMaterial.isPresent()) {
        continue;
      }

      final Material material = optionalMaterial.get();
      final int amount = levelPriceConfig.getInt(key);
      if (amount > 0) {
        prices.add(new ItemStack(material, amount));
      }
    }

    return prices;
  }
}
