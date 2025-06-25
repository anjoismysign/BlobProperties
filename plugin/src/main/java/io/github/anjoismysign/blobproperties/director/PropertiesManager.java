package blobproperties.director;

import us.mytheria.bloblib.entities.GenericManager;
import us.mytheria.blobproperties.BlobProperties;

public class PropertiesManager extends GenericManager<BlobProperties, PropertiesManagerDirector> {


    public PropertiesManager(PropertiesManagerDirector managerDirector) {
        super(managerDirector);
    }
}
