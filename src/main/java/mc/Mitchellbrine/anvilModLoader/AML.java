package mc.Mitchellbrine.anvilModLoader;

//import com.sun.javafx.fxml.expression.BinaryExpression;
import mc.Mitchellbrine.anvilModLoader.database.*;
//import mc.Mitchellbrine.anvilModLoader.util.ANSIColor;
import mc.Mitchellbrine.anvilModLoader.util.FileHelper;

//import net.minecraftforge.fml.relauncher.FMLInjectionData;
//import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
//import org.apache.commons.codec.BinaryEncoder;

import cpw.mods.fml.relauncher.FMLInjectionData;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

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

    private final String[] trustedSites = new String[]{"http://minecraft.curseforge.com","http://chickenbones.net","http://creeperrepo.net","http://planetminecraft.com","http://dl.dropboxusercontent.com/u/","http://copy.com", "https://github.com","https://bitbucket.org","http://ci.candicejoy.com:8080"};

    private static boolean filterEnabled = true;

    public static AML instance;

    public AML() {
        instance = this;
        new AMLDirectories();
        AMLDirectories.mkDir();
        instance.checkForConfig();
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

    private void checkForConfig() {
        File mainDirectory = (File) AMLDirectories.data()[0];

        File config = new File(mainDirectory,"config.cfg");

        if (!config.exists()) {
            try {
                PrintWriter writer = new PrintWriter(config);
                writer.println("#### The configuration file for AML ####");
                writer.println("");
                writer.println("# This config option allows you to disable AML's filter on trusted sites.");
                writer.println("# Although you CANNOT hold myself, Mitchellbrine, accountable for your actions, as can be seen in the LGPLv2.1 license AML uses,");
                writer.println("# No support or help of any kind will be given to those who disable the filter. I cannot be help liable and I do not want people ");
                writer.println("# possibly destroying others' computers and blaming me. So, I have given fair warning before you decide to switch it off.");
                writer.println("# -Mitchellbrine");
                writer.println("");
                writer.println("enableFilter=true");

                writer.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }



            try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(config)));

            String s;

            while ((s = reader.readLine()) != null) {
                if (s.startsWith("enableFilter=")) {
                    filterEnabled = Boolean.parseBoolean(s.substring(s.indexOf("enableFilter=")));
                    break;
                }
            }

                reader.close();

                if (!filterEnabled) {
                    /*AMLProperties.logger.warn(ANSIColor.ANSI_RED + "YOU HAVE DISABLED THE FILTER OF TRUSTED SITES ON AML." + ANSIColor.ANSI_RESET);
                    AMLProperties.logger.warn(ANSIColor.ANSI_RED + "Unfortunately, this means all support has been VOIDED " + ANSIColor.ANSI_RESET);
                    AMLProperties.logger.warn(ANSIColor.ANSI_RED + "until you re-enable the filter. Sorry! -Mitchellbrine" + ANSIColor.ANSI_RESET); */
                    //AMLProperties.logger.warn("0100011001010101010000100100000101010010");
                    AMLProperties.logger.warn("AML Loading...");
                } else {
                    //AMLProperties.logger.warn("0100011001010101010000100100000101010011");
                    AMLProperties.logger.info("AML Loading...");
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }

    }

    /**
     * Build databases is the method we can call before any loading because it has nothing to do with Minecraft itself!
     */
    public void buildDatabase() {

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
                        boolean isAbsolute = false;

                        if (s.startsWith("--") || !s.contains(":")) continue;

                        int startingSubstring = 0;

                        if (s.startsWith("*")) {
                            isCorrectVersion = true;
                            startingSubstring++;
                        } else if (s.startsWith("+")) {
                            isConfig = true;
                            startingSubstring++;
                        } else if (s.startsWith("$")) {
                            isAbsolute = true;
                        }

                        int startingSubstring2 = s.indexOf(":") + 1;

                        if (s.contains(": ")) {
                            startingSubstring2++;
                        }

                        if (!isConfig && !isAbsolute) {

                            if (filterEnabled) {
                                boolean isTrusted = false;

                                for (String site : trustedSites) {
                                    if (s.substring(startingSubstring2).startsWith(site)) {
                                        isTrusted = true;
                                    }
                                }

                                if (!isTrusted) continue;

                            }

                            ModVersion version = new ModVersion(s.substring(startingSubstring, s.indexOf(":")), new URL(s.substring(startingSubstring2)));

                            database.getVersions().add(version);

                            if (isCorrectVersion && database.getCurrentVersion() == null) {
                                database.setCurrentVersion(version);
                            }
                            database.setDatabaseName(file.getName().substring(0, file.getName().lastIndexOf(".")));
                        } else if (!isAbsolute){
                            for (ModVersion version : database.getVersions()) {
                                if (version.getVersion().equalsIgnoreCase(s.substring(startingSubstring, s.indexOf(":")))) {

                                    if (filterEnabled) {
                                        boolean isTrusted = false;

                                        for (String site : trustedSites) {
                                            if (s.substring(startingSubstring2).startsWith(site)) {
                                                isTrusted = true;
                                            }
                                        }

                                        if (!isTrusted) continue;

                                    }

                                    version.setConfigLocation(new URL(s.substring(startingSubstring2)));
                                }
                            }
                        } else {
                            for (ModVersion version : database.getVersions()) {
                                if (version.getVersion().equalsIgnoreCase(s.substring(startingSubstring, s.indexOf(":")))) {

                                    if (filterEnabled) {
                                        boolean isTrusted = false;

                                        for (String site : trustedSites) {
                                            if (s.substring(startingSubstring2).startsWith(site)) {
                                                isTrusted = true;
                                            }
                                        }

                                        if (!isTrusted) continue;

                                    }

                                    version.setAbsoluteLocation(new URL(s.substring(startingSubstring2)));
                                }
                            }
                        }


                    }

                    reader.close();

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
            if (!mod.getName().endsWith(".zip") && !mod.getName().endsWith(".jar")) {
                mod.delete();
                continue;
            }

            if (!mod.getName().contains("-")) continue;

            int hyphenLoc = mod.getName().indexOf("-");

            boolean hasRepo = false;

            for (ModDatabase database : databases) {
                if (database.getDatabaseName().equalsIgnoreCase(mod.getName().substring(0, hyphenLoc))) hasRepo = true;
            }

            if (hasRepo) {

                ZipFile zip = new ZipFile(mod);
                Enumeration<? extends ZipEntry> entries = zip.entries();

                boolean containsCoreMod = false;

                while (entries.hasMoreElements()) {
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
                } else {
                    ModDatabase database = null;
                    for (ModDatabase database1 : databases) {
                        if (database1.getDatabaseName().equalsIgnoreCase(mod.getName().substring(0,hyphenLoc))) {
                            database = database1;
                        }
                    }

                    if (database != null) {
                        if (database.getCurrentVersion() != null) {
                            if (!mod.getName().substring(hyphenLoc + 1).equalsIgnoreCase(database.getCurrentVersion().toString())) {
                                mod.delete();
                            }
                        }
                    }
                }
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
                    File absoluteLocation = null;
                    if (recommended.getAbsoluteFile() != null) {
                        absoluteLocation = new File(recommended.getDirectory(), "" + recommended.getAbsoluteFile().getFile().substring(recommended.getAbsoluteFile().getFile().lastIndexOf("/")));

                        if (!absoluteLocation.exists()) {
                            absoluteLocation.mkdirs();
                            FileHelper.download(recommended.getAbsoluteFile().openStream(), 0, absoluteLocation);
                        }
                    }

                        if (absoluteLocation != null) {
                            FileHelper.unzip(absoluteLocation,(File) FMLInjectionData.data()[6]);
                            AMLProperties.logger.info("Loaded absolute for " + recommended.getVersion() + " from database" + database.getDatabaseName().toLowerCase());
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
                    File absoluteLocation = null;
                    if (latest.getAbsoluteFile() != null) {
                        absoluteLocation = new File(latest.getDirectory(), "" + latest.getAbsoluteFile().getFile().substring(latest.getAbsoluteFile().getFile().lastIndexOf("/")));

                        if (!absoluteLocation.exists()) {
                            absoluteLocation.mkdirs();
                            FileHelper.download(latest.getAbsoluteFile().openStream(), 0, absoluteLocation);
                        }
                    }

                    if (absoluteLocation != null) {
                        FileHelper.unzip(absoluteLocation,(File) FMLInjectionData.data()[6]);
                        AMLProperties.logger.info("Loaded absolute for " + latest.getVersion() + " from database" + database.getDatabaseName().toLowerCase());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    }


}
