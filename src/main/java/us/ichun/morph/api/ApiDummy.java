package us.ichun.morph.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

public class ApiDummy implements IApi
{
    @Override
    public boolean hasMorph(String playerName, boolean isClient)
    {
        return false;
    }

    @Override
    public float morphProgress(String playerName, boolean isClient)
    {
        return 1.0F;
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
    public void forceDemorph(EntityPlayerMP player){}

    @Override
    public boolean isMorphApi()
    {
        return false;
    }
}
