package io.github.anjoismysign.blobproperties.entities;

import io.github.anjoismysign.bloblib.entities.inventory.BlobInventoryTracker;
import io.github.anjoismysign.blobproperties.entities.publicproperty.SimpleInternalProperty;

public record InventoryOperation(BlobInventoryTracker tracker,
                                 SimpleInternalProperty property) {
}
