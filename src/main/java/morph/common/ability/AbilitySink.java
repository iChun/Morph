package morph.common.ability;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import morph.api.Ability;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class AbilitySink extends Ability {

    public boolean isInWater;

	@Override
	public String getType() 
	{
		return "sink";
	}

	@Override
	public void tick() 
	{
        boolean flying = false;
        if(getParent() instanceof EntityPlayer)
        {
            flying = ((EntityPlayer)getParent()).capabilities.isFlying;
        }

        if((getParent().isInWater() || getParent().handleLavaMovement()))
		{
            if(getParent().isCollidedHorizontally)
            {
                getParent().motionY = 0.07D;
            }
            else if(getParent().motionY > -0.07D && !flying)
            {
                getParent().motionY = -0.07D;
            }
		}
        if(!getParent().isInWater() && isInWater && !flying)
        {
            getParent().motionY = 0.32D;
        }
        isInWater = getParent().isInWater();
	}

	@Override
	public void kill() 
	{
	}

	@Override
	public Ability clone() 
	{
		return new AbilitySink();
	}

	@Override
	public void postRender() {}

	@Override
	public void save(NBTTagCompound tag) {}

	@Override
	public void load(NBTTagCompound tag) {}

	@SideOnly(Side.CLIENT)
	@Override
	public ResourceLocation getIcon() 
	{
		return iconResource;
	}
	
	public static final ResourceLocation iconResource = new ResourceLocation("morph", "textures/icon/sink.png");

}
