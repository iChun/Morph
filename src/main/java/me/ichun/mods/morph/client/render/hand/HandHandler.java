package me.ichun.mods.morph.client.render.hand;

import com.google.gson.Gson;
import com.mojang.blaze3d.matrix.MatrixStack;
import me.ichun.mods.ichunutil.common.util.IOUtil;
import me.ichun.mods.morph.api.morph.MorphInfo;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.morph.MorphHandler;
import me.ichun.mods.morph.common.resource.ResourceHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

    public static HandHandler instance;

    private MorphInfo lastMorphInfo;
    private float lastPartialTick;

    @SubscribeEvent
    public void onRenderHand(RenderHandEvent event) //if we're getting the event, the config has already assigned us;
    {
        Minecraft mc = Minecraft.getInstance();
        if(!mc.player.removed) //we need to cache this as the hand may be rendered even in the death screen.
        {
            lastMorphInfo = MorphHandler.INSTANCE.getMorphInfo(mc.player);
            lastPartialTick = event.getPartialTicks();
        }
    }

    //Returns true if we have to override and render the hand.
    public boolean renderHand(PlayerRenderer renderer, MatrixStack stack, IRenderTypeBuffer buffer, int light, AbstractClientPlayerEntity player, ModelRenderer arm, ModelRenderer armwear)
    {
        //Check if this is the player, and we have the player's morph info.
        if(player == Minecraft.getInstance().getRenderViewEntity() && lastMorphInfo != null)
        {
            MorphInfo info = lastMorphInfo;
            float partialTick = lastPartialTick;
            float skinAlpha = info.getMorphSkinAlpha(partialTick);
            if(skinAlpha > 0F || info.isMorphed()) // if we're supposed to override the hand render
            {
                ModelRenderer[] handParts;

                HandSide handSide = renderer.entityModel.bipedRightArm == arm ? HandSide.RIGHT : HandSide.LEFT;

                float morphProg = info.getMorphProgress(partialTick);
                if(morphProg < 1F) //still morphing, transition may be required.
                {
                    float transitionProg = info.getTransitionProgressSine(partialTick);
                    if(transitionProg <= 0F)
                    {

                    }
                    else if(transitionProg >= 1F)
                    {

                    }
                    else
                    {

                    }
                }
                else //morph completed, just use nextState's entity instance
                {

                }

                return true;
            }
        }

        return false;
    }

    public static void setState(boolean allowed)
    {
        if(allowed)
        {
            if(instance == null)
            {
                MinecraftForge.EVENT_BUS.register(instance = new HandHandler());
            }
        }
        else
        {
            if(instance != null)
            {
                MinecraftForge.EVENT_BUS.unregister(instance);
                instance = null;
            }
        }
    }

    @Nullable
    private static HandInfo getHandInfo(Class<? extends EntityModel> clz)
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
            if(MODEL_HAND_INFO.containsKey(info.modelClass))
            {
                Morph.LOGGER.warn("Hand Info for {} already exists!", info.modelClass);
            }
            MODEL_HAND_INFO.put(info.modelClass, info);
        }

        Morph.LOGGER.info("Loaded {} Hand Info(s)", MODEL_HAND_INFO.size());
    }
}
