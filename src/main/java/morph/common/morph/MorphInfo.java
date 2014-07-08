package morph.common.morph;

import morph.api.Ability;
import morph.common.ability.AbilityHandler;
import morph.common.packet.PacketMorphInfo;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;

public class MorphInfo 
{
	public String playerName;
	public MorphState prevState;
	
	public MorphState nextState;
	
	private boolean morphing; //if true, increase progress
	public int morphProgress; //up to 80, 3 sec sound files, 0.5 sec between sounds where the skin turns black
	
	public ArrayList<Ability> morphAbilities = new ArrayList<Ability>(); 
	
	public MorphInfo(){playerName="";}
	
	public boolean flying;
	public boolean sleeping;

    public double healthOffset;
    public double preMorphHealth;
	
	public boolean firstUpdate;
	
	public MorphInfo(String name, MorphState prev, MorphState next)
	{
		playerName = name;
		
		prevState = prev;
		
		nextState = next;
		
		morphing = false;
		morphProgress = 0;
		
		flying = false;
		sleeping = false;
		firstUpdate = true;

        healthOffset = 0.0D;
        preMorphHealth = 20.0D;
	}
	
	public void setMorphing(boolean flag)
	{
		morphing = flag;
	}
	
	public boolean getMorphing()
	{
		return morphing;
	}
	
	public PacketMorphInfo getMorphInfoAsPacket()
	{
        return new PacketMorphInfo(playerName, morphing, morphProgress, prevState != null, nextState != null, prevState != null ? prevState.getTag() : null, nextState != null ? nextState.getTag() : null, flying);
	}

	public void writeNBT(NBTTagCompound tag)
	{
		tag.setString("playerName", playerName);
		
		tag.setInteger("dimension", nextState.entInstance.dimension);
		
		tag.setTag("nextState", nextState.getTag());
		
		tag.setBoolean("isFlying", flying);

        tag.setDouble("healthOffset", healthOffset);
        tag.setDouble("preMorphHealth", preMorphHealth);
	}
	
	public void readNBT(NBTTagCompound tag)
	{
		playerName = tag.getString("playerName");
		
		World dimension = DimensionManager.getWorld(tag.getInteger("dimension"));
		
		if(dimension == null)
		{
			dimension = DimensionManager.getWorld(0);
		}
		
		nextState = new MorphState(dimension, playerName, playerName, null, false);
		
		nextState.readTag(dimension, tag.getCompoundTag("nextState"));
		
		morphing = true;
		morphProgress = 80;
		
		ArrayList<Ability> newAbilities = AbilityHandler.getEntityAbilities(nextState.entInstance.getClass());
		morphAbilities = new ArrayList<Ability>();
		for(Ability ability : newAbilities)
		{
			try
			{
				morphAbilities.add(ability.clone());
			}
			catch(Exception e1)
			{
			}
		}
		
		flying = tag.getBoolean("isFlying");

        healthOffset = tag.getDouble("healthOffset");
        preMorphHealth = tag.getDouble("preMorphHealth");
	}
}

