package mc.Mitchellbrine.anvilModLoader.database;

import java.io.File;
import java.net.URL;

/**
 * Created by Mitchellbrine on 2015.
 */
public class ModVersion {

    private URL fileLocation;
    private String version;
    private File versionDirectory;
    private URL configLocation;

    public ModVersion(String versionName, URL fileLocation) {
        this.version = versionName;
        this.fileLocation = fileLocation;
    }

    public File getDirectory() { return this.versionDirectory; }

    public URL getFile() {
        return this.fileLocation;
    }

    public URL getConfig() {
        return this.configLocation;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersionDirectory(File directory) {
        this.versionDirectory = directory;
    }

    public void setConfigLocation(URL configLocation) {
        this.configLocation = configLocation;
    }

}
