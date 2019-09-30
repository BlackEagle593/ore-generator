package de.eaglefamily.oregenerator.generator;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

public class GeneratorProbability {

  private final Configuration config;
  private final Random random = new Random();
  private final Map<Integer, Map<Material, Double>> probabilities = Maps.newHashMap();

  /**
   * Creates an instance of generator probability.
   *
   * @param config the config
   */
  public GeneratorProbability(final Configuration config) {
    this.config = config;
  }

  Map<Material, Double> getProbabilities(final int level) {
    return probabilities.computeIfAbsent(level, l -> {
      Map<Material, Double> probabilities = new HashMap<>();

      final String pricesKey = "probabilities";
      final ConfigurationSection probabilitiesConfig =
          config.getConfigurationSection(pricesKey + "." + level);
      if (probabilitiesConfig == null) {
        return probabilities;
      }

      for (final String key : probabilitiesConfig.getKeys(false)) {
        final Optional<Material> optionalMaterial = Enums.getIfPresent(Material.class, key);
        if (!optionalMaterial.isPresent()) {
          continue;
        }

        final Material material = optionalMaterial.get();
        final double probability = probabilitiesConfig.getDouble(key);
        if (probability > 0) {
          probabilities.put(material, probability);
        }
      }
      return probabilities;
    });
  }

  Material getRandomOre(final int level) {
    final Map<Material, Double> probabilities = getProbabilities(level);
    Set<Map.Entry<Material, Double>> probabilityEntries = probabilities.entrySet();

    final double randomValue = random.nextDouble();
    double sum = 0;

    for (Map.Entry<Material, Double> probabilityEntry : probabilityEntries) {
      sum += probabilityEntry.getValue();
      if (sum > randomValue) {
        return probabilityEntry.getKey();
      }
    }

    return null;
  }
}
