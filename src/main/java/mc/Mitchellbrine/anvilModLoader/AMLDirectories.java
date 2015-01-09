package mc.Mitchellbrine.anvilModLoader;

import net.minecraftforge.fml.relauncher.FMLInjectionData;

import java.io.File;

/**
 * Created by Mitchellbrine on 2015.
 */
public class AMLDirectories {

    static File configDirectory;
    static File databaseDirectory;

    public AMLDirectories() {
        configDirectory = new File(getAMLDirectory(),"/config");
        databaseDirectory = new File(getAMLDirectory(),"/databases");
    }

    public static File getAMLDirectory() {
        return new File(((File)FMLInjectionData.data()[6]),"/AnvilModLoader");
    }

    public static Object[] data() {
        return new Object[]{getAMLDirectory(), configDirectory, databaseDirectory};
    }

    public static void mkDir() {
        if (!configDirectory.exists()) configDirectory.mkdirs();
        if (!databaseDirectory.exists()) databaseDirectory.mkdirs();
    }

}
