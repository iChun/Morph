package me.ichun.mods.morph.common.resource;

import com.google.gson.*;
import me.ichun.mods.morph.api.biomass.BiomassUpgradeInfo;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.morph.nbt.NbtModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
                    Morph.LOGGER.info("Extracted {} Morph files.", extractFiles(true));

                    FileUtils.writeStringToFile(extractedMarker.toFile(), "", StandardCharsets.UTF_8);
                }

                loadNbtModifiers();
                //TODO mob characteristics/upgrades?
                loadBiomassUpgrades(); //TODO propagate the upgrades to the players if there are players connected

                updateServerSession();
            }
            catch(IOException e)
            {
                Morph.LOGGER.fatal("Error initialising Morph resources!", e);
                return false;
            }
        }
        return init;
    }

    public static int extractFiles(boolean overwrite) throws IOException
    {
        int i = 0;
        InputStream in = Morph.class.getResourceAsStream("/mobsupport.zip");
        if(in != null)
        {
            ZipInputStream zipStream = new ZipInputStream(in);
            ZipEntry entry = null;

            while((entry = zipStream.getNextEntry()) != null)
            {
                Path path = morphDir.resolve(entry.getName());
                if(!overwrite && Files.exists(path) && Files.size(path) > 3L)
                {
                    continue;
                }

                if(entry.isDirectory())
                {
                    if(!Files.exists(path))
                    {
                        Files.createDirectories(path);
                    }
                }
                else
                {
                    FileOutputStream out = new FileOutputStream(path.toFile());

                    byte[] buffer = new byte[8192];
                    int len;
                    while((len = zipStream.read(buffer)) != -1)
                    {
                        out.write(buffer, 0, len);
                    }
                    out.close();

                    i++;
                }
            }
            zipStream.close();
        }
        return i;
    }

    public static void updateServerSession()
    {
        MorphHandler.BIOMASS_UPGRADES_SESSION.clear();
        MorphHandler.BIOMASS_UPGRADES_SESSION.putAll(MorphHandler.BIOMASS_UPGRADES);
    }

    public static void loadBiomassUpgrades()
    {
        MorphHandler.BIOMASS_UPGRADES.clear();

        readBiomassUpgrades(morphDir.resolve("biomass"));

        Morph.LOGGER.info("Loaded {} Biomass Upgrade(s)", MorphHandler.BIOMASS_UPGRADES.size());
    }

    private static void readBiomassUpgrades(Path path)
    {
        try
        {
            if(!Files.exists(path))
            {
                Files.createDirectories(path);
            }

            String json = FileUtils.readFileToString(path.resolve("upgrades.json").toFile(), "UTF-8");
            readBiomassUpgradesJson(json);
        }
        catch(IOException | JsonSyntaxException e)
        {
            Morph.LOGGER.error("Error reading directory for Biomass Upgrades: {}", path);
            e.printStackTrace();
        }
    }

    private static void readBiomassUpgradesJson(String json) throws JsonSyntaxException
    {
        BiomassUpgradeInfos upgradeInfo = GSON.fromJson(json, BiomassUpgradeInfos.class);
        if(upgradeInfo.upgrades == null)
        {
            Morph.LOGGER.warn("No Biomass Upgrades defined!");
            return;
        }

        for(BiomassUpgradeInfo upgrade : upgradeInfo.upgrades)
        {
            if(upgrade.id == null)
            {
                Morph.LOGGER.warn("Biomass Upgrade with no ID!");
                continue;
            }

            MorphHandler.BIOMASS_UPGRADES.put(upgrade.id, upgrade);
        }

        int count = MorphHandler.BIOMASS_UPGRADES.size();

        MorphHandler.BIOMASS_UPGRADES.entrySet().removeIf(e -> e.getValue().parentId == null && !e.getValue().id.equals("biomass_capacity") || !MorphHandler.BIOMASS_UPGRADES.containsKey(e.getValue().parentId));

        if(count != MorphHandler.BIOMASS_UPGRADES.size())
        {
            Morph.LOGGER.warn("Biomass Upgrades with no parent ID removed!");
        }
    }
    private static class BiomassUpgradeInfos{ public BiomassUpgradeInfo[] upgrades; }


    public static void loadNbtModifiers()
    {
        MorphHandler.NBT_MODIFIERS.clear();

        //        serialiseModifiers();

        scourNbtDirectory(morphDir.resolve("nbt"));

        Morph.LOGGER.info("Loaded {} NBT Modifier(s)", MorphHandler.NBT_MODIFIERS.size());
    }

    private static void scourNbtDirectory(Path path)
    {
        try
        {
            if(!Files.exists(path))
            {
                Files.createDirectories(path);
            }

            Files.list(path).forEach(p -> {
                if(Files.isDirectory(p))
                {
                    scourNbtDirectory(p);
                }
                else if(p.getFileName().toString().endsWith(".json"))
                {
                    File file = p.toFile();
                    try
                    {
                        String json = FileUtils.readFileToString(file, "UTF-8");
                        if(!readNbtJson(json))
                        {
                            Morph.LOGGER.error("Error reading NBT Modifier file, no forClass: {}", file);
                        }
                    }
                    catch(IOException | JsonSyntaxException e)
                    {
                        Morph.LOGGER.error("Error reading NBT Modifier file: {}", file);
                        e.printStackTrace();
                    }
                    catch(ClassNotFoundException ignored){}
                }
            });
        }
        catch(IOException e)
        {
            Morph.LOGGER.error("Error reading directory for NBT Modifiers: {}", path);
            e.printStackTrace();
        }
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
