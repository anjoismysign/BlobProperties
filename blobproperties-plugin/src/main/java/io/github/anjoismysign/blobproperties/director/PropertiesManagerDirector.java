package io.github.anjoismysign.blobproperties.director;

import io.github.anjoismysign.bloblib.entities.BlobPHExpansion;
import io.github.anjoismysign.bloblib.entities.GenericManagerDirector;
import io.github.anjoismysign.blobproperties.BlobProperties;
import io.github.anjoismysign.blobproperties.director.manager.ConfigManager;
import io.github.anjoismysign.blobproperties.director.manager.ListenerManager;
import io.github.anjoismysign.blobproperties.entity.ProprietorPlaceholderExpansion;

public class PropertiesManagerDirector extends GenericManagerDirector<BlobProperties> {
    private final LegacyFileManager legacyFileManager;

    public PropertiesManagerDirector(BlobProperties blobPlugin) {
        super(blobPlugin);
        registerBlobMessage("es_es/blobproperties_lang");
        registerBlobInventory("es_es/PublicPropertyBuilder");
        registerTranslatableSnippet("es_es/blobproperties_translatable_snippets");
        registerTranslatableBlock("es_es/blobproperties_translatable_blocks");
        registerBlobInventory("PublicPropertyBuy", "es_es/PublicPropertyBuy");
        registerBlobInventory("PublicPropertyHome", "es_es/PublicPropertyHome");
        legacyFileManager = new LegacyFileManager(this);
        addManager("ItemStackManager", new ItemStackManager(this));
        addManager("ConfigManager", new ConfigManager(this));
        addManager("PartyManager", InternalPartyManager.getInstance(this));
        addManager("ProprietorManager", new SimpleInstanceProprietorManager(this));
        addManager("ListenerManager", new ListenerManager(this));
        instantiateProprietorExpansion();
    }

    @Override
    public void reload() {
        getListenerManager().reload();
        PropertyShardManager.getInstance().reload();
    }

    @Override
    public void unload() {
        getProprietorManager().unload();
    }

    public final InternalPartyManager getPublicPartyManager() {
        return getManager("PartyManager", InternalPartyManager.class);
    }

    public final SimpleInstanceProprietorManager getProprietorManager() {
        return getManager("ProprietorManager", SimpleInstanceProprietorManager.class);
    }

    public final ConfigManager getConfigManager() {
        return getManager("ConfigManager", ConfigManager.class);
    }

    public final ListenerManager getListenerManager() {
        return getManager("ListenerManager", ListenerManager.class);
    }

    public final ItemStackManager getItemStackManager() {
        return getManager("ItemStackManager", ItemStackManager.class);
    }

    public final PropertyShardManager getPropertyShardManager() {
        return PropertyShardManager.getInstance();
    }

    public LegacyFileManager getLegacyFileManager() {
        return legacyFileManager;
    }

    private void instantiateProprietorExpansion() {
        ProprietorPlaceholderExpansion expansion = new ProprietorPlaceholderExpansion(this);
        if (!isPlaceholderAPIEnabled())
            return;
        BlobPHExpansion phExpansion = new BlobPHExpansion(this.getPlugin(), "proprietor");
        expansion.consumer().accept(phExpansion);
    }
}