package blobproperties.director.manager;

import us.mytheria.blobproperties.director.PropertiesManager;
import us.mytheria.blobproperties.director.PropertiesManagerDirector;
import us.mytheria.blobproperties.listeners.PublicPropertyBuy;
import us.mytheria.blobproperties.listeners.PublicPropertyHome;
import us.mytheria.blobproperties.listeners.PublicPropertyListener;
import us.mytheria.blobproperties.listeners.PublicProprietorListener;

public class ListenerManager extends PropertiesManager {
    private final PublicPropertyListener publicPropertyListener;
    private final PublicProprietorListener publicIndependentListener;
    private final PublicPropertyBuy publicPropertyBuy;
    private final PublicPropertyHome publicPropertyHome;

    public ListenerManager(PropertiesManagerDirector director) {
        super(director);
        publicPropertyListener = new PublicPropertyListener(director);
        publicIndependentListener = PublicProprietorListener.getInstance(director);
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

    public PublicProprietorListener getPublicIndependentListener() {
        return publicIndependentListener;
    }

    public PublicPropertyBuy getPublicPropertyBuy() {
        return publicPropertyBuy;
    }

    public PublicPropertyHome getPublicPropertyHome() {
        return publicPropertyHome;
    }
}
