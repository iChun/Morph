package me.ichun.mods.morph.client.render.hand;

import com.google.gson.Gson;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.resource.ResourceHandler;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@OnlyIn(Dist.CLIENT)
public final class HandHandler
{
    private static final HashMap<Class<? extends EntityModel>, HandInfo> MODEL_HAND_INFO = new HashMap<>();
    private static final Gson GSON = new Gson();

    @Nullable
    public static HandInfo getHandInfo(Class<? extends EntityModel> clz)
    {
        if(MODEL_HAND_INFO.containsKey(clz))
        {
            return MODEL_HAND_INFO.get(clz);
        }
        HandInfo helper = null;
        Class clzz = clz.getSuperclass();
        if(clzz != EntityModel.class)
        {
            helper = getHandInfo(clzz);
            if(helper != null)
            {
                helper = GSON.fromJson(GSON.toJson(helper), helper.getClass());
            }
        }
        MODEL_HAND_INFO.put(clz, helper);
        return helper;
    }

    public static void loadHandInfos()
    {
        MODEL_HAND_INFO.clear();

        ArrayList<HandInfo> infos = new ArrayList<>();
        try
        {
            IOUtil.scourDirectoryForFiles(ResourceHandler.getMorphDir().resolve("hand"), p -> {
                if(p.getFileName().toString().endsWith(".json"))
                {
                    try
                    {
                        HandInfo handInfo = GSON.fromJson(FileUtils.readFileToString(p.toFile(), "UTF-8"), HandInfo.class);
                        if(handInfo.setup())
                        {
                            infos.add(handInfo);
                            return true;
                        }
                    }
                    catch(IOException e)
                    {
                        Morph.LOGGER.error("Error reading file: {}", p);
                        e.printStackTrace();
                    }
                    return false;
                }
                return false;
            });
        }
        catch(IOException e)
        {
            Morph.LOGGER.error("Error reading Hand Infos");
            e.printStackTrace();
        }
        for(HandInfo info : infos)
        {
            MODEL_HAND_INFO.put(info.modelClass, info);
        }

        Morph.LOGGER.info("Loaded {} Hand Info(s)", MODEL_HAND_INFO.size());
    }
}
