package mc.Mitchellbrine.anvilModLoader;

import mc.Mitchellbrine.anvilModLoader.database.*;
import mc.Mitchellbrine.anvilModLoader.util.FileHelper;

import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

//import cpw.fml.relauncher.FMLInjectionData;
//import cpw.fml.relauncher.IFMLLoadingPlugin;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Mitchellbrine on 2015.
 */
@IFMLLoadingPlugin.MCVersion("1.8")
@IFMLLoadingPlugin.Name("AnvilModLoader")
@IFMLLoadingPlugin.SortingIndex(2001)
public class AML implements IFMLLoadingPlugin {

    public static ArrayList<ModDatabase> databases = new ArrayList<ModDatabase>();

    public static AML instance;

    public AML() {
        instance = this;
        instance.buildDatabase();
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return AMLProperties.class.getName();
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    /**
     * Build databases is the method we can call before any loading because it has nothing to do with Minecraft itself!
     */
    public void buildDatabase() {

        new AMLDirectories();
        AMLDirectories.mkDir();

        // TODO: Do all the work for building databases

        File configDirectory = (File) AMLDirectories.data()[1];

        if (configDirectory.listFiles() != null) {
            for (File file : configDirectory.listFiles()) {
                if (file.getName().endsWith(".zip") || file.getName().endsWith(".jar")) continue;

                try {

                    ModDatabase database = new ModDatabase();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

                    String s;

                    while ((s = reader.readLine()) != null) {

                        boolean isCorrectVersion = false;
                        boolean isConfig = false;

                        if (s.startsWith("--") || !s.contains(":")) continue;

                        int startingSubstring = 0;

                        if (s.startsWith("*")) {
                            isCorrectVersion = true;
                            startingSubstring++;
                        } else if (s.startsWith("+")) {
                            isConfig = true;
                            startingSubstring++;
                        }

                        int startingSubstring2 = s.indexOf(":") + 1;

                        if (s.contains(": ")) {
                            startingSubstring2++;
                        }

                        if (!isConfig) {
                            ModVersion version = new ModVersion(s.substring(startingSubstring, s.indexOf(":")), new URL(s.substring(startingSubstring2)));

                            database.getVersions().add(version);

                            if (isCorrectVersion && database.getCurrentVersion() == null) {
                                database.setCurrentVersion(version);
                            }
                            database.setDatabaseName(file.getName().substring(0, file.getName().lastIndexOf(".")));
                        } else {
                            for (ModVersion version : database.getVersions()) {
                                if (version.getVersion().equalsIgnoreCase(s.substring(startingSubstring, s.indexOf(":")))) {
                                    version.setConfigLocation(new URL(s.substring(startingSubstring2)));
                                }
                            }
                        }


                    }

                    database.buildDirectories();

                    databases.add(database);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
    }

        try {
            obtainAllCorrectVersions();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    /**
     * This is where a majority of the work takes place, downloading new versions/mods
     */
    public void obtainAllCorrectVersions() throws IOException {
        File modsDir = new File((File)FMLInjectionData.data()[6],"/mods");
        for (File mod : modsDir.listFiles()) {
            if (mod == null) continue;
            if (mod.getName().contains("AnvilModLoader")) continue;
            if (!mod.getName().endsWith(".zip") && !mod.getName().endsWith(".jar"))  { mod.delete(); continue; }

            ZipFile zip = new ZipFile(mod);
            Enumeration<? extends ZipEntry> entries = zip.entries();

            boolean containsCoreMod = false;

            while(entries.hasMoreElements()){
                ZipEntry entry = entries.nextElement();
                InputStream stream = zip.getInputStream(entry);

                if (!entry.getName().endsWith(".MF")) continue;

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

                String s;

                while ((s = reader.readLine()) != null) {
                    if (s.contains("FMLCorePlugin:")) {
                        containsCoreMod = true;
                    }
                }

                reader.close();

            }

            if (!containsCoreMod) {
                mod.delete();
            }
        }

        for (ModDatabase database : databases) {

            if (database.getCurrentVersion() != null) {
                ModVersion recommended = database.getCurrentVersion();
                try {
                    File newFileLoc = new File(recommended.getDirectory(),database.getDatabaseName().toLowerCase() + "-" + recommended.getVersion() + ".jar");
                    if (!newFileLoc.exists()) {
                        FileHelper.download(recommended.getFile().openStream(), 0, newFileLoc);
                    }
                        FileHelper.copy(new FileInputStream(newFileLoc), new File((File) FMLInjectionData.data()[6], "/mods/" + database.getDatabaseName().toLowerCase() + "-" + recommended.getVersion() + ".jar"));
                        if (new File((File)FMLInjectionData.data()[6],"/mods/" + database.getDatabaseName().toLowerCase() + "-" + recommended.getVersion() + ".jar").exists()) {
                            AMLProperties.logger.info("Loaded version " + recommended.getVersion() + " from database " + database.getDatabaseName().toLowerCase());
                        }
                    File configLocation = null;
                    if (recommended.getConfig() != null) {
                        configLocation = new File(recommended.getDirectory(),"/config/" + recommended.getConfig().getFile().lastIndexOf("/"));

                        if (!configLocation.exists()) {
                            configLocation.mkdirs();
                            FileHelper.download(recommended.getConfig().openStream(), 0, configLocation);
                        }
                    }
                    if (configLocation != null) {
                        FileHelper.copy(new FileInputStream(configLocation),new File((File) FMLInjectionData.data()[6], "/config/" + configLocation.getName()));
                        AMLProperties.logger.info("Loaded configs for " + recommended.getVersion() + " from database " + database.getDatabaseName().toLowerCase());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                ModVersion latest = database.getVersions().get(0);
                try {
                    File newFileLoc =  new File(latest.getDirectory(), database.getDatabaseName().toLowerCase() + "-" + latest.getVersion() + ".jar");
                    if (!newFileLoc.exists()) {
                        FileHelper.download(latest.getFile().openStream(), 0, newFileLoc);
                    }
                    FileHelper.copy(new FileInputStream(new File(latest.getDirectory(), database.getDatabaseName().toLowerCase() + "-" + latest.getVersion() + ".jar")), new File((File) FMLInjectionData.data()[6], "/mods/" + database.getDatabaseName().toLowerCase() + "-" + latest.getVersion() + ".jar"));
                    if (new File((File)FMLInjectionData.data()[6],"/mods/" + database.getDatabaseName().toLowerCase() + "-" + latest.getVersion() + ".jar").exists()) {
                        AMLProperties.logger.info("Loaded version " + latest.getVersion() + " from database " + database.getDatabaseName().toLowerCase());
                    }
                    File configLocation = null;
                    if (latest.getConfig() != null) {
                        configLocation = new File(latest.getDirectory(),"/config/" + latest.getConfig().getFile().lastIndexOf("/"));

                        if (!configLocation.exists()) {
                            configLocation.mkdirs();
                            FileHelper.download(latest.getConfig().openStream(),0,configLocation);
                        }
                    }
                    if (configLocation != null) {
                        FileHelper.copy(new FileInputStream(configLocation),new File((File) FMLInjectionData.data()[6], "/config/" + configLocation.getName()));
                        AMLProperties.logger.info("Loaded configs for " + latest.getVersion() + " from database " + database.getDatabaseName().toLowerCase());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    }


}
