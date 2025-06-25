package io.github.anjoismysign.blobproperties;

import us.mytheria.bloblib.managers.BlobPlugin;
import us.mytheria.bloblib.managers.IManagerDirector;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;

public class BlobProperties extends BlobPlugin {

    private static BlobProperties instance;
    private PropertiesManagerDirector director;
    private IManagerDirector proxy;
    private BlobPropertiesAPI api;


    @Override
    public void onEnable() {
        instance = this;
        this.director = new PropertiesManagerDirector(this);
        this.api = BlobPropertiesAPI.getInstance(director);
        this.proxy = director.proxy();
    }

    public static BlobProperties getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        director.unload();
    }

    @Override
    public IManagerDirector getManagerDirector() {
        return proxy;
    }

    public BlobPropertiesAPI getApi() {
        return api;
    }
}
