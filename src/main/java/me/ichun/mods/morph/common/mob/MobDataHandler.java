package me.ichun.mods.morph.common.mob;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import me.ichun.mods.morph.api.event.MorphLoadResourceEvent;
import me.ichun.mods.morph.api.mob.MobData;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.resource.ResourceHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class MobDataHandler
{
    private static final HashMap<ResourceLocation, MobData> MOB_DATA = new HashMap<>();
    private static final HashMap<ResourceLocation, MobData> MOB_DATA_FROM_MODS = new HashMap<>();

    public static void loadMobData()
    {
        MOB_DATA.clear();

        int filesProcessed = 0;

        try
        {
            filesProcessed = IOUtil.scourDirectoryForFiles(ResourceHandler.getMorphDir().resolve("mob"), p -> {
                if(p.getFileName().toString().endsWith(".json"))
                {
                    File file = p.toFile();
                    try
                    {
                        String json = FileUtils.readFileToString(file, "UTF-8");
                        if(readMobDataJson(json))
                        {
                            return true;
                        }
                        else
                        {
                            Morph.LOGGER.error("Error reading Mob Data file, no forEntity: {}", file);
                            return false;
                        }
                    }
                    catch(IOException | JsonSyntaxException e)
                    {
                        Morph.LOGGER.error("Error reading Mob Data file: {}", file);
                        e.printStackTrace();
                    }
                }
                return false;
            });
        }
        catch(IOException e)
        {
            Morph.LOGGER.error("Error loading Mob Data files.", e);
        }

        Morph.LOGGER.info("Loaded {} Mob Data(s) from {} files", MOB_DATA.size(), filesProcessed);

        readdModMobData();

        MinecraftForge.EVENT_BUS.post(new MorphLoadResourceEvent(MorphLoadResourceEvent.Type.MOB));
    }

    private static boolean readMobDataJson(String json) throws JsonSyntaxException
    {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(json).getAsJsonObject();
        if(jsonObject.has("forEntity"))
        {
            String resourceId = jsonObject.get("forEntity").getAsString();
            ResourceLocation rl = new ResourceLocation(resourceId);

            if(MOB_DATA.containsKey(rl))
            {
                Morph.LOGGER.warn("We already have another Mob Data for {}", resourceId);
            }

            try
            {
                EntityType<?> entType = ForgeRegistries.ENTITIES.getValue(rl);

                if(entType != null) //this entity type is registered, load it up
                {
                    MobData mobData = ResourceHandler.GSON.fromJson(json, MobData.class);
                    MOB_DATA.put(rl, mobData);
                }
            }
            catch(Throwable t)
            {
                Morph.LOGGER.error("Error deserialising Mob Data for {}", resourceId);
                t.printStackTrace();
            }
            return true;
        }
        return false;
    }

    public static void registerMobData(ResourceLocation rl, MobData data)
    {
        MOB_DATA_FROM_MODS.put(rl, data);

        if(MOB_DATA.containsKey(rl))
        {
            Morph.LOGGER.warn("We already have another Mob Data for {}. This is a mod-level override so we shall acknowledge it.", rl.toString());
        }

        MOB_DATA.put(rl, data);
    }

    private static void readdModMobData()
    {
        MOB_DATA.putAll(MOB_DATA_FROM_MODS);
    }

    @Nullable
    public static MobData getMobData(ResourceLocation rl)
    {
        return MOB_DATA.get(rl);
    }

    @Nullable
    public static MobData getMobData(LivingEntity living)
    {
        return getMobData(living.getType().getRegistryName());
    }
}
