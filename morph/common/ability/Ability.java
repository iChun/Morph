package morph.common.ability;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntitySquid;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
		parent = ent;
	}
	
	public EntityLivingBase getParent()
	{
		return parent;
	}
	
	public abstract String getType();
	public abstract void tick(); // called only when parent != null. parent is not necessarily a player
	public abstract void kill(); // called when the ability is finally removed, NOT when it is passed on with another morph.
	public abstract Ability clone();
	@SideOnly(Side.CLIENT)
	public abstract void postRender();
	
	static
	{
		//TODO complete for vanilla mobs
		mapAbilities(EntityBat.class, new AbilityFly());
		mapAbilities(EntityBlaze.class, new AbilityFly(), new AbilityFireImmunity());
		mapAbilities(EntityGhast.class, new AbilityFly(), new AbilityFireImmunity());
		mapAbilities(EntityDragon.class, new AbilityFly());
		mapAbilities(EntityWither.class, new AbilityFly(), new AbilityFireImmunity());
		mapAbilities(EntityChicken.class, new AbilityFloat(-0.1141748D, true));
		mapAbilities(EntitySquid.class, new AbilitySwim(false));
		mapAbilities(EntityPigZombie.class, new AbilityFireImmunity());
		mapAbilities(EntityMagmaCube.class, new AbilityFireImmunity());
		mapAbilities(EntitySkeleton.class, new AbilityFireImmunity());
		mapAbilities(EntitySpider.class, new AbilityClimb());
	}
	
	public static void mapAbilities(Class<? extends EntityLivingBase> entClass, Ability...abilities)
	{
		ArrayList<Ability> abilityList = abilityMap.get(entClass);
		if(abilityList == null)
		{
			abilityList = new ArrayList<Ability>();
			abilityMap.put(entClass, abilityList);
		}
		for(Ability ability : abilities)
		{
			boolean added = false;
			for(int i = 0; i < abilityList.size(); i++)
			{
				Ability ab = abilityList.get(i);
				if(ab.getType().equals(ability.getType()))
				{
					abilityList.remove(i);
					abilityList.add(i, ability);
					added = true;
				}
			}
			if(!added)
			{
				abilityList.add(ability);
			}
		}
	}
}
