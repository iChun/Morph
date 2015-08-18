package me.ichun.mods.morph.common.core;

import me.ichun.mods.morph.client.core.TickHandlerClient;
import me.ichun.mods.morph.client.model.ModelHandler;
import me.ichun.mods.morph.client.morph.MorphInfoClient;
import me.ichun.mods.morph.common.Morph;
import me.ichun.mods.morph.common.handler.PlayerMorphHandler;
import me.ichun.mods.morph.common.morph.MorphInfo;
import me.ichun.mods.morph.common.morph.MorphState;
import me.ichun.mods.morph.common.packet.PacketGuiInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import us.ichun.mods.ichunutil.client.keybind.KeyBind;
import us.ichun.mods.ichunutil.client.keybind.KeyEvent;
import us.ichun.mods.ichunutil.common.core.event.RendererSafeCompatibilityEvent;

import java.util.Map;

public class EventHandler
{
    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SideOnly(Side.CLIENT)
    public void onRendererSafeCompatibility(RendererSafeCompatibilityEvent event)
    {
        for(Object obj : Minecraft.getMinecraft().getRenderManager().entityRenderMap.entrySet())
        {
            Map.Entry<Class, Render> e = (Map.Entry<Class, Render>)obj;
            Class clz = e.getKey();
            if(EntityLivingBase.class.isAssignableFrom(clz))
            {
                ModelHandler.dissectForModels(clz, e.getValue());
            }
            ModelHandler.mapPlayerModels();
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onKeyEvent(KeyEvent event)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if(event.keyBind.isPressed())
        {
            if(event.keyBind.equals(Morph.config.keySelectorUp) || event.keyBind.equals(Morph.config.keySelectorDown) || event.keyBind.equals(Morph.config.keySelectorLeft) || event.keyBind.equals(Morph.config.keySelectorRight))
            {
                Morph.proxy.tickHandlerClient.handleSelectorNavigation(event.keyBind);
            }
            else if(event.keyBind.equals(Morph.config.keySelectorSelect) || (event.keyBind.keyIndex == mc.gameSettings.keyBindAttack.getKeyCode() && event.keyBind.isMinecraftBind()))
            {
                if(Morph.proxy.tickHandlerClient.selectorShow)
                {
                    Morph.proxy.tickHandlerClient.selectorShow = false;
                    Morph.proxy.tickHandlerClient.selectorShowTimer = TickHandlerClient.SELECTOR_SHOW_TIME - Morph.proxy.tickHandlerClient.selectorShowTimer;
                    Morph.proxy.tickHandlerClient.selectorScrollHoriTimer = TickHandlerClient.SELECTOR_SCROLL_TIME;

                    MorphState selectedState = Morph.proxy.tickHandlerClient.getCurrentlySelectedMorphState();
                    MorphInfoClient info = Morph.proxy.tickHandlerClient.morphsActive.get(mc.thePlayer.getCommandSenderName());

                    if(selectedState != null && (info != null && !info.nextState.currentVariant.equals(selectedState.currentVariant) || info == null && !selectedState.currentVariant.playerName.equalsIgnoreCase(mc.thePlayer.getCommandSenderName())))
                    {
                        Morph.channel.sendToServer(new PacketGuiInput(selectedState.currentVariant.thisVariant.identifier, 0, false));
                    }
                }
            }
            else if(event.keyBind.equals(Morph.config.keySelectorCancel) || (event.keyBind.keyIndex == mc.gameSettings.keyBindUseItem.getKeyCode() && event.keyBind.isMinecraftBind()))
            {
                if(Morph.proxy.tickHandlerClient.selectorShow)
                {
                    if(mc.currentScreen instanceof GuiIngameMenu)
                    {
                        mc.displayGuiScreen(null);
                    }
                    Morph.proxy.tickHandlerClient.selectorShow = false;
                    Morph.proxy.tickHandlerClient.selectorShowTimer = TickHandlerClient.SELECTOR_SHOW_TIME - Morph.proxy.tickHandlerClient.selectorShowTimer;
                    Morph.proxy.tickHandlerClient.selectorScrollHoriTimer = TickHandlerClient.SELECTOR_SCROLL_TIME;
                }
            }
            else if(event.keyBind.equals(Morph.config.keySelectorRemoveMorph) || event.keyBind.keyIndex == Keyboard.KEY_DELETE)
            {
                if(Morph.proxy.tickHandlerClient.selectorShow)
                {
                    MorphState selectedState = Morph.proxy.tickHandlerClient.getCurrentlySelectedMorphState();
                    MorphInfoClient info = Morph.proxy.tickHandlerClient.morphsActive.get(mc.thePlayer.getCommandSenderName());

                    if(selectedState != null && !selectedState.currentVariant.thisVariant.isFavourite && ((info == null || !info.nextState.currentVariant.thisVariant.identifier.equalsIgnoreCase(selectedState.currentVariant.thisVariant.identifier)) && !selectedState.currentVariant.playerName.equalsIgnoreCase(mc.thePlayer.getCommandSenderName())))
                    {
                        Morph.channel.sendToServer(new PacketGuiInput(selectedState.currentVariant.thisVariant.identifier, 2, false));
                    }
                }
            }
        }
        else
        {
            //RADIAL MENU
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onMouseEvent(MouseEvent event)
    {
        if(Morph.proxy.tickHandlerClient.selectorShow)
        {
            int k = event.dwheel;
            if(k != 0)
            {
                KeyBind bind;
                if(GuiScreen.isShiftKeyDown())
                {
                    if(k > 0)
                    {
                        bind = Morph.config.keySelectorLeft;
                    }
                    else
                    {
                        bind = Morph.config.keySelectorRight;
                    }
                }
                else
                {
                    if(k > 0)
                    {
                        bind = Morph.config.keySelectorUp;
                    }
                    else
                    {
                        bind = Morph.config.keySelectorDown;
                    }
                }
                Morph.proxy.tickHandlerClient.handleSelectorNavigation(bind);
                event.setCanceled(true);
            }
        }
        //        else if(Morph.proxy.tickHandlerClient.radialShow)
        //        {
        //            Morph.proxy.tickHandlerClient.radialDeltaX += event.dx / 100D;
        //            Morph.proxy.tickHandlerClient.radialDeltaY += event.dy / 100D;
        //
        //            double mag = Math.sqrt(Morph.proxy.tickHandlerClient.radialDeltaX * Morph.proxy.tickHandlerClient.radialDeltaX + Morph.proxy.tickHandlerClient.radialDeltaY * Morph.proxy.tickHandlerClient.radialDeltaY);
        //            if(mag > 1.0D)
        //            {
        //                Morph.proxy.tickHandlerClient.radialDeltaX /= mag;
        //                Morph.proxy.tickHandlerClient.radialDeltaY /= mag;
        //            }
        //        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        if(event.world.isRemote && event.world instanceof WorldClient)
        {
            //Clean up the Morph States and stuff like that to prevent mem leaks.
            for(MorphInfoClient info : Morph.proxy.tickHandlerClient.morphsActive.values())
            {
                info.clean();
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event)
    {
        if(!event.entityLiving.worldObj.isRemote)
        {
            if(event.source.getEntity() instanceof EntityPlayerMP && event.entityLiving != event.source.getEntity())
            {
                EntityPlayerMP player = (EntityPlayerMP)event.source.getEntity();

                if(PlayerMorphHandler.getInstance().canPlayerMorph(player))
                {
                    EntityLivingBase living = event.entityLiving; //entity to acquire

                    if(event.entityLiving instanceof EntityPlayerMP)
                    {
                        EntityPlayerMP player1 = (EntityPlayerMP)event.entityLiving;

                        MorphInfo info = Morph.proxy.tickHandlerServer.morphsActive.get(player1.getCommandSenderName());
                        if(info != null)
                        {
                            if(info.isMorphing() && info.prevState != null)
                            {
                                living = info.prevState.getEntInstance(player1.worldObj);
                            }
                            else
                            {
                                living = info.nextState.getEntInstance(player1.worldObj);
                            }
                        }
                    }

                    PlayerMorphHandler.getInstance().acquireMorph(player, living, Morph.config.instaMorph == 1, true);
                }
            }
        }
    }
}
