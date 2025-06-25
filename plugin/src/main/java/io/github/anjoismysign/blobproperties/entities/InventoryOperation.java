package blobproperties.entities;

import us.mytheria.bloblib.entities.inventory.BlobInventoryTracker;
import us.mytheria.blobproperties.entities.publicproperty.PublicProperty;

public record InventoryOperation(BlobInventoryTracker tracker,
                                 PublicProperty property) {
}
