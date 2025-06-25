package io.github.anjoismysign.blobproperties.director;

import us.mytheria.bloblib.entities.BlobPHExpansion;
import us.mytheria.bloblib.entities.GenericManagerDirector;
import us.mytheria.bloblib.entities.ObjectDirector;
import io.github.anjoismysign.blobproperties.BlobProperties;
import io.github.anjoismysign.blobproperties.director.manager.ConfigManager;
import io.github.anjoismysign.blobproperties.director.manager.ListenerManager;
import io.github.anjoismysign.blobproperties.entities.ProprietorPlaceholderExpansion;
import io.github.anjoismysign.blobproperties.entities.publicproperty.PublicProperty;
import io.github.anjoismysign.blobproperties.entities.publicproperty.PublicPropertyAdminCommand;
import io.github.anjoismysign.blobproperties.entities.publicproperty.PublicPropertyCommand;

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
        addManager("PropertyManager", new PropertyManager(this));
        addManager("ConfigManager", new ConfigManager(this));
        addManager("PublicPartyManager", new PublicPartyManager(this));
        addManager("ProprietorManager", new SimpleInstanceProprietorManager(this));
        addDirector("PublicProperty", file -> PublicProperty.fromFile(file, this), false);
        PublicPropertyCommand publicPropertyCommand = PublicPropertyCommand.getInstance(this);
        PublicPropertyAdminCommand publicPropertyAdminCommand = PublicPropertyAdminCommand.getInstance(this);
        getPublicPropertyDirector().addNonAdminChildTabCompleter(publicPropertyCommand::tabCompleter);
        getPublicPropertyDirector().addNonAdminChildCommand(publicPropertyCommand::command);
        getPublicPropertyDirector().addAdminChildCommand(publicPropertyAdminCommand::command);
        getPublicPropertyDirector().addAdminChildTabCompleter(publicPropertyAdminCommand::tabCompleter);
        addManager("ListenerManager", new ListenerManager(this));
        instantiateProprietorExpansion();
    }

    @Override
    public void reload() {
        getListenerManager().reload();
        getPublicPropertyDirector().getObjectManager().values()
                .forEach(getPropertyManager()::removePublicProperty);
        getPublicPropertyDirector().reload();
        getPublicPropertyDirector().whenObjectManagerFilesLoad(manager -> {
            getProprietorManager().reload();
        });
    }

    @Override
    public void unload() {
        getProprietorManager().unload();
    }

    public final PublicPartyManager getPublicPartyManager() {
        return getManager("PublicPartyManager", PublicPartyManager.class);
    }

    public final ObjectDirector<PublicProperty> getPublicPropertyDirector() {
        return getDirector("PublicProperty", PublicProperty.class);
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

    public final PropertyManager getPropertyManager() {
        return getManager("PropertyManager", PropertyManager.class);
    }

    public LegacyFileManager getLegacyFileManager() {
        return legacyFileManager;
    }

    private void instantiateProprietorExpansion() {
        ProprietorPlaceholderExpansion expansion = new ProprietorPlaceholderExpansion(this);
        if (!isPlaceholderAPIEnabled())
            return;
        BlobPHExpansion x = new BlobPHExpansion(this.getPlugin(), "proprietor");
        expansion.consumer().accept(x);
    }
}