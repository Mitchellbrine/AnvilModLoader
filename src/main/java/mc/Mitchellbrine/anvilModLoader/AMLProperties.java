package mc.Mitchellbrine.anvilModLoader;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import cpw.fml.common.DummyModContainer;
//import cpw.fml.common.LoadController;
//import cpw.fml.common.ModMetadata;
//import cpw.fml.common.FMLInitializationEvent;
//import cpw.fml.common.FMLPostInitializationEvent;

import java.util.Arrays;

/**
 * Created by Mitchellbrine on 2015.
 */
public class AMLProperties extends DummyModContainer {

    public static Logger logger = LogManager.getLogger("AML");

    public AMLProperties() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "AML";
        meta.name = "AnvilModLoader";
        meta.version = "1.0";
        meta.credits = "JamOORev members who supported me";
        meta.authorList = Arrays.asList("Mitchellbrine");
        meta.description = "The mod that makes modpacks!";
        meta.url = "";
        meta.updateUrl = "";
        meta.screenshots = new String[0];
        meta.logoFile = "/aml-logo.png";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    @Subscribe
    public void init(FMLInitializationEvent evt) {

    }


    @Subscribe
    public void postInit(FMLPostInitializationEvent evt) {

    }

}
