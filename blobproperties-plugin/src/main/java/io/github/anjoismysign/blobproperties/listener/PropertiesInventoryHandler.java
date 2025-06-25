package io.github.anjoismysign.blobproperties.listener;

import io.github.anjoismysign.blobproperties.director.PropertiesManagerDirector;

public class PropertiesInventoryHandler extends ProprietorListener {
    private final PropertiesInventoryProcessor processor;

    public PropertiesInventoryHandler(PropertiesManagerDirector director) {
        super(director);
        processor = new PropertiesInventoryProcessor(director);
    }

    public PropertiesInventoryProcessor getProcessor() {
        return processor;
    }
}
