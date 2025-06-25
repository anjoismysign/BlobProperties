package io.github.anjoismysign.blobproperties.director;

import io.github.anjoismysign.bloblib.entities.GenericManager;
import io.github.anjoismysign.blobproperties.BlobProperties;

public class PropertiesManager extends GenericManager<BlobProperties, PropertiesManagerDirector> {
    public PropertiesManager(PropertiesManagerDirector managerDirector) {
        super(managerDirector);
    }
}
