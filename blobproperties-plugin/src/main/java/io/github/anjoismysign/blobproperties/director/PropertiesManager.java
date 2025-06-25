package io.github.anjoismysign.blobproperties.director;

import us.mytheria.bloblib.entities.GenericManager;
import io.github.anjoismysign.blobproperties.BlobProperties;

public class PropertiesManager extends GenericManager<BlobProperties, PropertiesManagerDirector> {


    public PropertiesManager(PropertiesManagerDirector managerDirector) {
        super(managerDirector);
    }
}
