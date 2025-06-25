package io.github.anjoismysign.blobproperties.director;

import io.github.anjoismysign.bloblib.utilities.ResourceUtil;
import io.github.anjoismysign.blobproperties.BlobProperties;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class LegacyFileManager {
    private final PropertiesManagerDirector director;
    private final File path = new File("plugins/BlobProperties");
    private final File items = new File(path.getPath() + "/items.yml");

    public LegacyFileManager(PropertiesManagerDirector director) {
        this.director = director;
        loadFiles();
    }

    public void loadFiles() {
        try {
            if (!path.exists()) path.mkdir();
            ///////////////////////////////////////////
            if (!items.exists()) items.createNewFile();
            ResourceUtil.updateYml(path, "/tempEquipment.yml", "items.yml", items, BlobProperties.getInstance());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public YamlConfiguration getYml(File f) {
        return YamlConfiguration.loadConfiguration(f);
    }

    public File getItems() {
        return items;
    }
}