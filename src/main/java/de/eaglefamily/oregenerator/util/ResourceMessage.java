package de.eaglefamily.oregenerator.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ResourceMessage {

  private final Plugin plugin;
  private final Map<Locale, ResourceBundle> resourceBundles = new HashMap<>();
  private final Map<String, MessageFormat> messageFormats = new HashMap<>();

  /**
   * Creates an instance of resource message.
   *
   * @param plugin the plugin
   */
  public ResourceMessage(final Plugin plugin) {
    this.plugin = plugin;
  }

  /**
   * Gets the localized string.
   *
   * @param key       the key of the message
   * @param locale    the locale
   * @param arguments the arguments
   * @return the localized string
   */
  public String getString(final String key, final Locale locale, final Object... arguments) {
    final ResourceBundle resourceBundle = getResourceBundle(locale);
    return getMessageFormat(resourceBundle.getString(key), locale).format(arguments);
  }

  /**
   * Gets the localized string for a player.
   *
   * @param player    the player
   * @param key       the key of the message
   * @param arguments the arguments
   * @return the localized string for the player
   */
  public String getString(final Player player, final String key, final Object... arguments) {
    final String[] localeCode = player.getLocale().split("_");
    final Locale locale = new Locale(localeCode[0], localeCode[1]);
    return getString(key, locale, arguments);
  }

  /**
   * Sends a message to the command sender.
   *
   * @param sender    the sender
   * @param key       the key of the message
   * @param arguments the arguments
   */
  public void sendMessage(final CommandSender sender, final String key, final Object... arguments) {
    if (sender instanceof Player) {
      sender.sendMessage(getString((Player) sender, key, arguments));
    } else {
      sender.sendMessage(getString(key, Locale.ENGLISH, arguments));
    }
  }

  private ResourceBundle getResourceBundle(final Locale locale) {
    return resourceBundles
        .computeIfAbsent(locale, key -> ResourceBundle.getBundle(plugin.getName(), locale));
  }

  private MessageFormat getMessageFormat(final String pattern, final Locale locale) {
    return messageFormats.computeIfAbsent(pattern, key -> new MessageFormat(key, locale));
  }
}
