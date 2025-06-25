package io.github.anjoismysign.blobproperties.entity;

import io.github.anjoismysign.bloblib.entities.inventory.BlobInventoryTracker;
import org.jetbrains.annotations.NotNull;

public record InventoryOperation(@NotNull BlobInventoryTracker tracker,
                                 @NotNull InternalProperty property) {
}
