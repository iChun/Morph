package me.ichun.mods.morph.common.resource;

import com.google.gson.*;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import me.ichun.mods.morph.api.biomass.BiomassUpgradeInfo;
import me.ichun.mods.morph.client.render.hand.HandHandler;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.morph.nbt.NbtModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class ResourceHandler
{
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Gson GSON_MINIFY = new GsonBuilder().disableHtmlEscaping().create();

    private static Path morphDir;
    private static boolean init;
    public static synchronized boolean init()
    {
        if(!init)
        {
            init = true;

            try
            {
                morphDir = FMLPaths.CONFIGDIR.get().resolve(Morph.MOD_ID);
                if(!Files.exists(morphDir)) Files.createDirectory(morphDir);

                Path extractedMarker = morphDir.resolve("files.extracted");
                if(!Files.exists(extractedMarker)) //presume we haven't extracted anything yet
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

                loadNbtModifiers();

                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> HandHandler::loadHandInfos); //load the hand infos. Only required on the client

                //TODO mob traits/upgrades?
//                loadBiomassUpgrades(); //TODO propagate the upgrades to the players if there are players connected
                //TODO just load up the biomass upgrades when server starts. sync with client.
                //TODO delay loading up abilities till after init stage to allow IMC registries of abilities from other mods
            }
            catch(IOException e)
            {
                Morph.LOGGER.fatal("Error initialising Morph resources!", e);
                return false;
            }
        }
        return init;
    }

    public static Path getMorphDir()
    {
        return morphDir;
    }

    public static void loadBiomassUpgrades()
    {
        MorphHandler.BIOMASS_UPGRADES.clear();

        HashMap<String, BiomassUpgradeInfo> upgradeMap = new HashMap<>();
        try
        {
            IOUtil.scourDirectoryForFiles(morphDir.resolve("biomass"), p -> {
                if(p.getFileName().toString().endsWith(".json"))
                {
                    File file = p.toFile();
                    try
                    {
                        BiomassUpgradeInfo upgradeInfo = GSON.fromJson(FileUtils.readFileToString(file, "UTF-8"), BiomassUpgradeInfo.class);
                        if(upgradeInfo.id == null)
                        {
                            Morph.LOGGER.error("Biomass Upgrade has no id: {}", file);
                        }
                        else if(upgradeInfo.parentId == null)
                        {
                            Morph.LOGGER.error("Biomass Upgrade has no parent id: {}", file);
                        }
                        else
                        {
                            upgradeMap.put(upgradeInfo.id, upgradeInfo);
                        }
                        return true;
                    }
                    catch(IOException | JsonSyntaxException e)
                    {
                        Morph.LOGGER.error("Error reading Biomass Upgrade file: {}", file);
                        e.printStackTrace();
                    }
                }
                return false;
            });
        }
        catch(IOException e)
        {
            Morph.LOGGER.error("Error loading Biomass files.", e);
        }

        upgradeMap.entrySet().removeIf(e -> {
            if(!e.getValue().parentId.equals("root") && !upgradeMap.containsKey(e.getValue().parentId))
            {
                Morph.LOGGER.error("Removing biomass upgrade with ID {} as we cannot find their parent with ID {}", e.getValue().id, e.getValue().parentId);
                return true;
            }
            return false;
        });

        MorphHandler.BIOMASS_UPGRADES.putAll(upgradeMap);

        Morph.LOGGER.info("Loaded {} Biomass Upgrade(s)", MorphHandler.BIOMASS_UPGRADES.size());
    }

    public static void loadNbtModifiers()
    {
        MorphHandler.NBT_MODIFIERS.clear();

        //        serialiseModifiers();

        try
        {
            IOUtil.scourDirectoryForFiles(morphDir.resolve("nbt"), p -> {
                if(p.getFileName().toString().endsWith(".json"))
                {
                    File file = p.toFile();
                    try
                    {
                        String json = FileUtils.readFileToString(file, "UTF-8");
                        if(readNbtJson(json))
                        {
                            return true;
                        }
                        else
                        {
                            Morph.LOGGER.error("Error reading NBT Modifier file, no forClass: {}", file);
                            return false;
                        }
                    }
                    catch(IOException | JsonSyntaxException e)
                    {
                        Morph.LOGGER.error("Error reading NBT Modifier file: {}", file);
                        e.printStackTrace();
                    }
                    catch(ClassNotFoundException ignored){}
                }
                return false;
            });
        }
        catch(IOException e)
        {
            Morph.LOGGER.error("Error loading NBT Modifier files.", e);
        }

        Morph.LOGGER.info("Loaded {} NBT Modifier(s)", MorphHandler.NBT_MODIFIERS.size());
    }

    private static boolean readNbtJson(String json) throws ClassNotFoundException, JsonSyntaxException
    {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(json).getAsJsonObject();
        if(jsonObject.has("forClass"))
        {
            String className = jsonObject.get("forClass").getAsString();

            Class clz = Class.forName(className);

            if(MorphHandler.NBT_MODIFIERS.containsKey(clz))
            {
                Morph.LOGGER.warn("We already have another NBT Modifier for {}", clz.getName());
            }

            try
            {
                NbtModifier nbtModifier = GSON.fromJson(json, NbtModifier.class);
                MorphHandler.NBT_MODIFIERS.put(clz, nbtModifier);
            }
            catch(Throwable t)
            {
                Morph.LOGGER.error("Error deserialising NBT Modifier for {}", clz.getName());
                t.printStackTrace();
            }
            return true;
        }
        return false;
    }

    private static void serialiseModifiers()
    {
        Path file = morphDir.resolve("nbt").resolve("LivingEntity.json");

        NbtModifier modifier = new NbtModifier();
        modifier.forClass = LivingEntity.class.getName();
        String[] strip = new String[] { "Health", "HurtTime", "HurtByTimestamp", "DeathTime", "AbsorptionAmount", "FallFlying", "SleepingX", "SleepingY", "SleepingZ", "Brain" };
        for(String s : strip)
        {
            NbtModifier.Modifier mod = new NbtModifier.Modifier();
            mod.key = s;
            mod.strip = true;
            modifier.modifiers.add(mod);
        }

        NbtModifier.Modifier hatsMod = new NbtModifier.Modifier();
        hatsMod.key = "ForgeCaps";

        NbtModifier.Modifier partMod = new NbtModifier.Modifier();
        partMod.key = "hats:capability_hat";
        partMod.strip = true;

        hatsMod.nestedModifiers = new ArrayList<>();
        hatsMod.nestedModifiers.add(partMod);

        ArrayList<NbtModifier.Modifier> list = new ArrayList<>();
        list.add(hatsMod);

        modifier.modSpecificModifiers.put("hats", list);

        try
        {
            String json = GSON.toJson(modifier);
            FileUtils.writeStringToFile(file.toFile(), json, "UTF-8");
        }
        catch(IOException ignored){}
        catch(Throwable e1)
        {
            e1.printStackTrace();
        }
    }
}
