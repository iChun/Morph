package me.ichun.mods.morph.client.morph;

import me.ichun.mods.morph.common.morph.MorphInfo;
import me.ichun.mods.morph.common.morph.MorphState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MorphInfoClient extends MorphInfo
{
    public MorphInfoClient(EntityPlayer player, MorphState prevState, MorphState nextState)
    {
        super(player, prevState, nextState);
    }
}
