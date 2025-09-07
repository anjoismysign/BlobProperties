package io.github.anjoismysign.blobproperties.listener;

import io.github.anjoismysign.blobproperties.BlobProperties;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import org.bukkit.event.Listener;

public class ProprietorListener implements Listener {
    private final PropertiesManagerDirector director;

    public ProprietorListener(PropertiesManagerDirector director) {
        this.director = director;
    }

    public PropertiesManagerDirector getManagerDirector() {
        return director;
    }
    public BlobProperties getPlugin() {
        return BlobProperties.getInstance();
    }
}
