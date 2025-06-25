package io.github.anjoismysign.blobproperties;

import io.github.anjoismysign.holoworld.asset.DataAsset;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.bloblib.api.BlobLibTranslatableAPI;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableBlock;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatablePositionable;
import io.github.anjoismysign.bloblib.utilities.TextColor;
import io.github.anjoismysign.blobproperties.BlobPropertiesAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface Property extends DataAsset {

    @NotNull
    String displayName(@Nullable Player player);

    @NotNull
    default List<String> lore(Player player){
        TranslatableBlock block = Objects.requireNonNull(
                BlobLibTranslatableAPI.getInstance().getTranslatableBlock(identifier() + "_lore", player.getLocale()),
                "No TranslatableBlock under " + type().getName() + " '" + identifier() + "_lore'");
        List<String> lore = new ArrayList<>();
        block.get().forEach(s -> {
            s = s.replace("%price%",
                            BlobPropertiesAPI.getInstance().format(price()))
                    .replace("%world%", world().getName())
                    .replace("%key%", identifier())
                    .replace("%identifier%", identifier())
                    .replace("%displayName%", displayName(player))
                    .replace("%containers%", totalContainers() + "")
                    .replace("%rows%", totalRows() + "")
                    .replace("%slots%", totalContainers() * 9 + "");
            s = TextColor.PARSE(s);
            lore.add(s);
        });
        return lore;
    }

    @NotNull default World world() {
        return Objects.requireNonNull(
                outside("en_us").get().toLocation().getWorld(),
                "No world found for " + type().getName() + " '" + identifier() + "'");
    }

    double price();

    long totalContainers();

    long totalRows();

    @NotNull
    default TranslatablePositionable inside(@NotNull String locale) {
        return Objects.requireNonNull(
                BlobLibTranslatableAPI.getInstance().getTranslatablePositionable(identifier() + "_inside", locale),
                "No TranslatablePositionable under " + type().getName() + " '" + identifier() + "_inside'");
    }

    @NotNull
    default TranslatablePositionable outside(@NotNull String locale) {
        return Objects.requireNonNull(
                BlobLibTranslatableAPI.getInstance().getTranslatablePositionable(identifier() + "_outside", locale),
                "No TranslatablePositionable under " + type().getName() + " '" + identifier() + "_outside'");
    }

    /**
     * Teleports the player outside the property
     *
     * @param player the player to teleport
     * @return true if the player was teleported, false otherwise
     */
    boolean getOutside(Player player);

    /**
     * Teleports the player to the inside of the property
     *
     * @param player the player to teleport
     * @return true if the player was teleported, false otherwise
     */
    boolean getInside(Player player);

    @NotNull
    PropertyType type();
}
