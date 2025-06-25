package blobproperties.listeners;

import org.bukkit.event.Listener;
import us.mytheria.blobproperties.BlobProperties;
import us.mytheria.blobproperties.director.PropertiesManagerDirector;
import us.mytheria.blobproperties.director.SimpleInstanceProprietorManager;

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
        return director.getPlugin();
    }
}
