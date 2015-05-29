package us.ichun.morph.common.core;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import us.ichun.morph.api.IApi;

public class ApiImpl implements IApi
{
    @Override
    public boolean hasMorph(String playerName, boolean isClient)
    {
        return false;
    }

    @Override
    public float morphProgress(String playerName, boolean isClient)
    {
        return 0;
    }

    @Override
    public float timeToCompleteMorph()
    {
        return 80;
    }

    @Override
    public EntityLivingBase getPrevMorphEntity(String playerName, boolean isClient)
    {
        return null;
    }

    @Override
    public EntityLivingBase getMorphEntity(String playerName, boolean isClient)
    {
        return null;
    }

    @Override
    public void forceDemorph(EntityPlayerMP player)
    {
    }

    @Override
    public boolean isMorphApi()
    {
        return true;
    }
}
