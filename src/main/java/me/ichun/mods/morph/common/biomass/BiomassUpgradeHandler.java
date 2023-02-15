package me.ichun.mods.morph.common.biomass;

import com.google.gson.JsonSyntaxException;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import me.ichun.mods.morph.api.biomass.BiomassUpgradeInfo;
import me.ichun.mods.morph.api.event.MorphLoadResourceEvent;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.resource.ResourceHandler;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class BiomassUpgradeHandler
{
    public static final HashMap<String, BiomassUpgradeInfo> BIOMASS_UPGRADES = new HashMap<>(); //TODO do I want to do it like this?

    public static void loadBiomassUpgrades()
    {
        BIOMASS_UPGRADES.clear();

        HashMap<String, BiomassUpgradeInfo> upgradeMap = new HashMap<>();
        try
        {
            IOUtil.scourDirectoryForFiles(ResourceHandler.getMorphDir().resolve("biomass"), p -> {
                if(p.getFileName().toString().endsWith(".json"))
                {
                    File file = p.toFile();
                    try
                    {
                        BiomassUpgradeInfo upgradeInfo = ResourceHandler.GSON.fromJson(FileUtils.readFileToString(file, "UTF-8"), BiomassUpgradeInfo.class);
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
                    catch(IOException | JsonSyntaxException | IllegalStateException e)
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

        BIOMASS_UPGRADES.putAll(upgradeMap);

        Morph.LOGGER.info("Loaded {} Biomass Upgrade(s)", BIOMASS_UPGRADES.size());

        MinecraftForge.EVENT_BUS.post(new MorphLoadResourceEvent(MorphLoadResourceEvent.Type.BIOMASS));
    }
}
