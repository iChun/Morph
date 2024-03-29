package me.ichun.mods.morph.common.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import me.ichun.mods.morph.api.mob.trait.Trait;
import me.ichun.mods.morph.client.render.hand.HandHandler;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.mob.MobDataHandler;
import me.ichun.mods.morph.common.mob.TraitHandler;
import me.ichun.mods.morph.common.morph.nbt.NbtHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceHandler
{
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Trait.class, new TraitHandler.TraitDeserialiser())
            .setPrettyPrinting().disableHtmlEscaping().create();
    public static final Gson GSON_MINIFY = new GsonBuilder().disableHtmlEscaping().create();
    public static final int MOB_SUPPORT_VERSION = 2;


    private static Path morphDir;
    private static boolean init;
    public static synchronized boolean setupEnv()
    {
        if(!init)
        {
            init = true;

            try
            {
                morphDir = FMLPaths.CONFIGDIR.get().resolve(Morph.MOD_ID);
                if(!Files.exists(morphDir)) Files.createDirectory(morphDir);

                Path extractedMarker = morphDir.resolve(MOB_SUPPORT_VERSION + ".extracted");
                if(!Files.exists(extractedMarker)) //presume we haven't extracted anything yet
                {
                    extractFiles(extractedMarker);
                }
            }
            catch(IOException e)
            {
                Morph.LOGGER.fatal("Error initialising Morph resources!", e);
                return false;
            }
        }
        return init;
    }

    public static synchronized void extractFiles(Path extractedMarker) throws IOException
    {
        InputStream in = Morph.class.getResourceAsStream("/mobsupport.zip");
        if(in != null)
        {
            Morph.LOGGER.info("Extracted {} Morph-related files.", IOUtil.extractFiles(morphDir, in, true));
        }
        else
        {
            Morph.LOGGER.error("Error loading mobsupport.zip. InputStream was null.");
        }

        FileUtils.writeStringToFile(extractedMarker.toFile(), "", StandardCharsets.UTF_8);
    }

    public static synchronized void loadResources() //should be initialised in FMLLoadCompleteEvent stage
    {
        //This data is reliant on Class names.
        NbtHandler.loadNbtModifiers();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> HandHandler::loadHandInfos); //load the hand infos. Only required on the client

        //This data rely on entity type resource location
        MobDataHandler.loadMobData();

        //                loadBiomassUpgrades(); //TODO propagate the upgrades to the players if there are players connected
        //TODO just load up the biomass upgrades when server starts. sync with client.
    }

    public static Path getMorphDir()
    {
        return morphDir;
    }

    public static void reloadAllResources()
    {
        loadResources();
    }
}
