package morph.common.ability;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.passive.EntityBat;

public abstract class Ability 
{
	public final static String[] trackableAbilities = new String[] { "fly", "float", "swim", "fireImmunity" };
	public final static HashMap<Class<? extends EntityLivingBase>, ArrayList<Ability>> abilityMap = new HashMap<Class<? extends EntityLivingBase>, ArrayList<Ability>>();
	
	private EntityLivingBase parent;
	
	public Ability()
	{
		parent = null;
	}
	
	public void setParent(EntityLivingBase ent)
	{
		if(parent == null)
		{
			parent = ent;
		}
	}
	
	public EntityLivingBase getParent()
	{
		return parent;
	}
	
	public abstract String getType();
	public abstract void tick(); // called only when parent != null. parent is not necessarily a player
	public abstract void kill(); // called when the ability is finally removed, NOT when it is passed on with another morph.
	public abstract Ability clone();
	
	static
	{
		//TODO complete for vanilla mobs
		mapAbilities(EntityBat.class, new AbilityFly());
		mapAbilities(EntityBlaze.class, new AbilityFly());
		mapAbilities(EntityGhast.class, new AbilityFly());
		mapAbilities(EntityDragon.class, new AbilityFly());
		mapAbilities(EntityWither.class, new AbilityFly());
	}
	
	public static void mapAbilities(Class<? extends EntityLivingBase> entClass, Ability...abilities)
	{
		ArrayList<Ability> abilityList = new ArrayList<Ability>();
		for(Ability ability : abilities)
		{
			abilityList.add(ability);
		}
		abilityMap.put(entClass, abilityList);
	}
}
