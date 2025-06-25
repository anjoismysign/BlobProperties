package io.github.anjoismysign.blobproperties.listener;

import io.github.anjoismysign.blobproperties.BlobProperties;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import io.github.anjoismysign.blobproperties.director.SimpleInstanceProprietorManager;
import org.bukkit.event.Listener;

public class ProprietorListener implements Listener {
    private final PropertiesManagerDirector director;
    private final SimpleInstanceProprietorManager proprietorManager;

    public ProprietorListener(PropertiesManagerDirector director) {
        this.director = director;
        this.proprietorManager = director.getProprietorManager();
    }

    public PropertiesManagerDirector getManagerDirector() {
        return director;
    }

    public SimpleInstanceProprietorManager getProprietorManager() {
        return proprietorManager;
    }

    public BlobProperties getPlugin() {
        return BlobProperties.getInstance();
    }
}
