package mc.Mitchellbrine.anvilModLoader.database;

import mc.Mitchellbrine.anvilModLoader.AMLDirectories;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Mitchellbrine on 2015.
 */
public class ModDatabase {

    private String databaseName;

    private ArrayList<ModVersion> versions = new ArrayList<ModVersion>();

    private ModVersion currentVersion;

    private File databaseDirectory;

    public void setCurrentVersion(ModVersion newVersion) {
        if (!versions.contains(newVersion)) throw new IllegalArgumentException("The version given does not exist!");
        else
        this.currentVersion = newVersion;
    }

    public void setDatabaseName(String name) {
        this.databaseName = name;
    }

    public void setVersions(ArrayList<ModVersion> newVersions) {
        this.versions = newVersions;
    }

    public ModVersion getCurrentVersion() {
        return this.currentVersion;
    }

    public ArrayList<ModVersion> getVersions() {
        return this.versions;
    }

    public File getDatabaseDirectory() { return this.databaseDirectory; }

    public String getDatabaseName() {
        if (this.databaseDirectory == null) this.databaseDirectory = new File((File)AMLDirectories.data()[2],"/"+databaseName.toLowerCase());
        return this.databaseName;
    }

    public void buildDirectories() {
        File databaseFolder = new File((File)AMLDirectories.data()[2],"/"+databaseName.toLowerCase());

        if (!databaseFolder.exists()) databaseFolder.mkdirs();

        for (ModVersion version : versions) {
            File versionFolder = new File(databaseFolder,"/" + version.getVersion().toLowerCase());
            if (!versionFolder.exists()) { versionFolder.mkdirs(); }
            version.setVersionDirectory(versionFolder);
        }

        this.databaseDirectory = databaseFolder;

    }

}
