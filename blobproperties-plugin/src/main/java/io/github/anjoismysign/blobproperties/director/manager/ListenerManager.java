package io.github.anjoismysign.blobproperties.director.manager;

import io.github.anjoismysign.blobproperties.director.PropertiesManager;
import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;
import io.github.anjoismysign.blobproperties.listener.PublicPropertyBuy;
import io.github.anjoismysign.blobproperties.listener.PublicPropertyHome;
import io.github.anjoismysign.blobproperties.listener.PublicPropertyListener;
import io.github.anjoismysign.blobproperties.listener.PublicProprietorListener;

public class ListenerManager extends PropertiesManager {
    private final PublicPropertyListener publicPropertyListener;
    private final PublicProprietorListener publicProprietorListener;
    private final PublicPropertyBuy publicPropertyBuy;
    private final PublicPropertyHome publicPropertyHome;

    public ListenerManager(PropertiesManagerDirector director) {
        super(director);
        publicPropertyListener = new PublicPropertyListener(director);
        publicProprietorListener = PublicProprietorListener.getInstance(director);
        publicPropertyBuy = new PublicPropertyBuy(director);
        publicPropertyHome = new PublicPropertyHome(director);
    }

    @Override
    public void reload() {
        publicPropertyBuy.reload();
        publicPropertyHome.reload();
    }

    public PublicPropertyListener getPublicPropertyListener() {
        return publicPropertyListener;
    }

    public PublicProprietorListener getPublicProprietorListener() {
        return publicProprietorListener;
    }

    public PublicPropertyBuy getPublicPropertyBuy() {
        return publicPropertyBuy;
    }

    public PublicPropertyHome getPublicPropertyHome() {
        return publicPropertyHome;
    }
}
