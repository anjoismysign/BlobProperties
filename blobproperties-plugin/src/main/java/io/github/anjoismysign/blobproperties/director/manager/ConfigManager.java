package io.github.anjoismysign.blobproperties.director.manager;

import io.github.anjoismysign.bloblib.managers.BlobPlugin;
import io.github.anjoismysign.blobproperties.director.PropertiesManager;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import io.github.anjoismysign.blobproperties.numberformat.BlobTycoon;
import io.github.anjoismysign.blobproperties.numberformat.Formatter;
import io.github.anjoismysign.blobproperties.numberformat.Generic;
import io.github.anjoismysign.blobproperties.numberformat.Integers;
import io.github.anjoismysign.blobproperties.numberformat.Vault;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager extends PropertiesManager {

    private final Formatter formatter;

    private final long pendingInvitesExpiration;

    public ConfigManager(PropertiesManagerDirector director) {
        super(director);
        BlobPlugin main = getPlugin();
        main.reloadConfig();
        main.saveDefaultConfig();
        main.getConfig().options().copyDefaults(true);
        main.saveConfig();
        FileConfiguration config = main.getConfig();
        this.pendingInvitesExpiration = config.getLong("Options.PendingInvitesExpiration");
        String format = config.getString("Options.numberFormat");
        switch (format) {
            case "VAULT":
                this.formatter = new Vault();
                break;
            case "INTEGERS":
                this.formatter = new Integers();
                break;
            case "BLOBTYCOON":
                this.formatter = new BlobTycoon();
                break;
            case "GENERIC":
                this.formatter = new Generic();
                break;
            default:
                Bukkit.getLogger().info("[BlobProperties] Invalid number format in config.yml. Using default (GENERIC).");
                Bukkit.getLogger().info("[BlobProperties] Valid formats: INTEGERS, BLOBTYCOON, GENERIC");
                this.formatter = new Generic();
                break;
        }
    }

    public String format(double amount) {
        return formatter.format(amount);
    }

    public long getPendingInvitesExpiration() {
        return pendingInvitesExpiration;
    }
}
