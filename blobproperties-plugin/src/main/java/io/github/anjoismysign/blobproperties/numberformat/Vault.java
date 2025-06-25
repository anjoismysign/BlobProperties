package io.github.anjoismysign.blobproperties.numberformats;

import io.github.anjoismysign.bloblib.api.BlobLibEconomyAPI;
import io.github.anjoismysign.bloblib.vault.multieconomy.ElasticEconomy;
import net.milkbowl.vault.economy.IdentityEconomy;
import org.bukkit.Bukkit;

import java.util.Optional;

public class Vault implements Formatter {
    public Vault() {
    }

    public String format(double amount, Optional<String> currency) {
        ElasticEconomy economy = BlobLibEconomyAPI.getInstance().getElasticEconomy();
        if (economy.isAbsent())
            Bukkit.getLogger().severe("Vault is not installed, but is required for BlobProperties to work properly. Please install Vault and restart your server.");
        IdentityEconomy identityEconomy = economy.map(currency);
        return identityEconomy.format(amount);
    }
}
