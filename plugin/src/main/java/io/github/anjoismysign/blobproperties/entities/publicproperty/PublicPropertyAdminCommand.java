package blobproperties.entities.publicproperty;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.mytheria.bloblib.entities.BlobExecutor;
import us.mytheria.bloblib.entities.ExecutorData;
import us.mytheria.bloblib.utilities.TextColor;
import us.mytheria.blobproperties.director.ItemStackManager;
import us.mytheria.blobproperties.director.PropertiesManagerDirector;
import us.mytheria.blobproperties.director.PropertyManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PublicPropertyAdminCommand {
    private static PublicPropertyAdminCommand instance;
    private final PropertiesManagerDirector director;
    private final PropertyManager propertyManager;
    private final ItemStackManager itemStackManager;

    private PublicPropertyAdminCommand(PropertiesManagerDirector director) {
        this.director = director;
        this.propertyManager = director.getPropertyManager();
        this.itemStackManager = director.getItemStackManager();
    }

    public static PublicPropertyAdminCommand getInstance(PropertiesManagerDirector director) {
        if (instance == null) {
            if (director == null)
                throw new NullPointerException("injected dependency is null");
            PublicPropertyAdminCommand.instance = new PublicPropertyAdminCommand(director);
        }
        return instance;
    }

    public static PublicPropertyAdminCommand getInstance() {
        return getInstance(null);
    }

    public boolean command(ExecutorData data) {
        BlobExecutor executor = data.executor();
        CommandSender sender = data.sender();
        String[] args = data.args();
        HashMap<String, PublicProperty> properties = director.getPropertyManager().getPublicProperties();
        if (!executor.isInstanceOfPlayer(sender)) {
            return false;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            return false;
        }
        String arg = args[0];
        if (!arg.equalsIgnoreCase("manage"))
            return false;
        if (args.length <= 2) {
            debug(sender);
            return false;
        }
        String key = args[1];
        String subArg = args[2];
        PublicProperty property = properties.get(key);
        if (property == null) {
            sender.sendMessage("'" + key + "' is not a property");
            return true;
        }
        if (args.length == 3) {
            switch (subArg.toLowerCase()) {
                case "doormanager" -> {
                    player.getInventory().addItem(director.getItemStackManager().getPublicDoorManager(property));
                    player.sendMessage(ChatColor.GREEN + "You have been given a door manager for " + property.getKey());
                    return true;
                }
                case "inside" -> {
                    property.setInside(player.getLocation());
                    propertyManager.addPublicProperty(property);
                    player.sendMessage("Teleport set.");
                    propertyManager.saveProperty(property);
                    return true;
                }
                case "outside" -> {
                    property.setOutside(player.getLocation());
                    propertyManager.addPublicProperty(property);
                    player.sendMessage("Teleport set.");
                    propertyManager.saveProperty(property);
                    return true;
                }
                default -> {
                    return false;
                }
            }
        }
        if (args.length == 4) {
            if (subArg.equalsIgnoreCase("containermanager")) {
                int rows;
                try {
                    rows = Integer.parseInt(args[3]);
                    if (rows > 0 && rows < 7) {
                        player.getInventory().addItem(itemStackManager.getPublicContainerManager(property, rows));
                        player.sendMessage(ChatColor.GREEN + "You have been given a container manager for " + property.getKey());
                    } else {
                        sender.sendMessage("Rows must be between 1 and 6");
                    }
                    return true;

                } catch (NumberFormatException e) {
                    sender.sendMessage("Rows need to be a number");
                    return true;
                }
            }
            sender.sendMessage("This region is already a property.");
            return true;
        }
        return false;
    }

    private static void debug(CommandSender sender) {
        if (sender.hasPermission("blobproperties.debug")) {
            sender.sendMessage(translate("&6/publicproperty manage <region> inside"));
            sender.sendMessage(translate("&6/publicproperty manage <region> outside"));
            sender.sendMessage(translate("&6/publicproperty manage <region> doorManager"));
            sender.sendMessage(translate("&6/publicproperty manage <region> containerManager <rows>"));
        }
    }

    private static String translate(String message) {
        return TextColor.PARSE(message);
    }

    public List<String> tabCompleter(ExecutorData data) {
        String[] args = data.args();
        List<String> suggestions = new ArrayList<>();
        switch (args.length) {
            case 1 -> {
                suggestions.add("manage");
            }
            case 2 -> {
                String arg = args[0];
                if (!arg.equalsIgnoreCase("manage")) {
                    return null;
                }
                suggestions.addAll(propertyManager.getPublicProperties().keySet());
            }
            case 3 -> {
                String arg = args[1];
                PublicProperty property = propertyManager.getPublicProperty(arg);
                if (property == null)
                    return null;
                suggestions.add("doorManager");
                suggestions.add("inside");
                suggestions.add("outside");
                suggestions.add("containerManager");
            }
            case 4 -> {
                String subArg = args[1];
                if (subArg.equalsIgnoreCase("containermanager")) {
                    for (int i = 1; i < 7; i++) {
                        suggestions.add(String.valueOf(i));
                    }
                }
            }
        }
        return suggestions;
    }
}
