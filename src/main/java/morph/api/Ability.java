package morph.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Abstract ability class.
 * Think of it like the Entity class, extend it to make your own types.
 * Some abilities may seem more like traits, but let's just call it an ability for simplicity's sake.
 * Please take note that entities inherit their superclass' abilities.
 * @author iChun
 *
 */
public abstract class Ability 
{
	/**
	 * Ability parent field. Will be null for instances used in registration. Ability is then cloned and parent assigned later on.
	 */
	private EntityLivingBase parent;
	
	/**
	 * Basic constructor (but you didn't really need me to tell you that ;D )
	 */
	public Ability()
	{
		parent = null;
	}
	
	/**
	 * Function for mod mob support, with args.
	 */
	public Ability parse(String[] args) { return this; }
	
	/**
	 * Since parent is private it needs a setter.
	 * @param newParent
	 */
	public void setParent(EntityLivingBase ent)
	{
		parent = ent;
	}
	
	/**
	 * Get's the parent entity for this ability
	 * @return Entity the ability takes effect on
	 */
	public EntityLivingBase getParent()
	{
		return parent;
	}
	
	/**
	 * Each ability has to return a String type.
	 * This is used for comparison, saving, as well as construction/loading of Ability.
	 * Think of it like the way Minecraft registers entities.
	 * @return Ability type
	 */
	public abstract String getType();
	
	/**
	 * Ticks every world tick, basically an ability onUpdate, similar to Entity's onUpdate.
	 * Will only tick if getParent() is not null.
	 * Please remember that getParent is not necessarily a player.
	 */
	public abstract void tick();
	
	/**
	 * Called when the ability is finally removed when the parent demorphs or morphs into a state that does not have this ability type.
	 * This will NOT be called if the parent morphs into another morph that has this type of ability.
	 */
	public abstract void kill();
	
	/**
	 * Creates a copy of this ability for use with parents.
	 * As previously stated before the ability instance used during registration is a base so it needs to be cloned for use with parents.
	 */
	public abstract Ability clone();
	
	/**
	 * Saving of ability to NBTTagCompound. 
	 * Mainly used for synching Abilities between the client-server for mod mobs which do not use the API to add abilities.
	 * The ability type (getType()) is appended to nbt before function is called.
	 * @param NBTTagCompound saveData
	 */
	public abstract void save(NBTTagCompound tag);
	
	/**
	 * Loading of ability from NBTTagCompound.
	 * Mainly used to load custom fields from NBT.
	 * @param NBTTagCompound saveData
	 */
	public abstract void load(NBTTagCompound tag);
	
	/**
	 * Rendering to be done post-render.
	 * EG: Used by AbilitySwim to render air bubbles whilst on land.
	 */
	@SideOnly(Side.CLIENT)
	public abstract void postRender();
	
	/**
	 * Icon location for ability. Can be null.
	 * Mod's default icons are 32x32. Can be any resolution though.
	 * @return resourcelocation for icon
	 */
	@SideOnly(Side.CLIENT)
	public abstract ResourceLocation getIcon();
	
	@SideOnly(Side.CLIENT)
	public boolean entityHasAbility(EntityLivingBase living)
	{
		return true;
	}
	
	/**
	 * Registers the ability so the mod can look up the class when attempting to load Ability save data.
	 * Call this no later than PostInit.
	 * @param ability type
	 * @param AbilityClass
	 */
	public static void registerAbility(String name, Class<? extends Ability> clz)
	{
		try {
			Class.forName("morph.common.ability.AbilityHandler").getDeclaredMethod("registerAbility", String.class, Class.class).invoke(null, name, clz);
		} catch (Exception e) {
		}
	}

	/**
	 * Maps abilities to an Entity.
	 * Adds on to the previous ability list, so this allows you to add abilities to Entity classes which already have abilities mapped.
	 * However, only one ability of the same type is allowed for each entity. This method will overwrite abilities of the same type that were already mapped.
	 * This will also register new abilities which were not registered before (just in case).
	 * Call this no later than PostInit.
	 * @param entClass
	 * @param abilities
	 */
	public static void mapAbilities(Class<? extends EntityLivingBase> entClass, Ability...abilities)
	{
		try {
			Class.forName("morph.common.ability.AbilityHandler").getDeclaredMethod("mapAbilities", Class.class, Ability[].class).invoke(null, entClass, abilities);
		} catch (Exception e) {
		}
	}
	
	/**
	 * Superman's kryptonite.
	 * @param Entity class to remove ability from
	 * @param Ability type
	 */
	public static void removeAbility(Class<? extends EntityLivingBase> entClass, String type)
	{
		try {
			Class.forName("morph.common.ability.AbilityHandler").getDeclaredMethod("removeAbility", Class.class, String.class).invoke(null, entClass, type);
		} catch (Exception e) {
		}
	}
	
	/**
	 * Checks to see if the entity class has a mapped ability type.
	 * @param entClass
	 * @param Ability type
	 * @return Entity class has ability type
	 */
	public static boolean hasAbility(Class<? extends EntityLivingBase> entClass, String type)
	{
		try {
			return (Boolean)Class.forName("morph.common.ability.AbilityHandler").getDeclaredMethod("hasAbility", Class.class, String.class).invoke(null, entClass, type);
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * The following are helper functions to create abilities packaged with Morph.
	 * Use these to ability instances for mapping vanilla abilities to mobs.
	 * Self explanatory.
	 * @return
	 */
	public static Ability getNewAbilityClimb()
	{
		try {
			return (Ability)Class.forName("morph.common.ability.AbilityHandler").getDeclaredMethod("getNewAbilityClimb").invoke(null);
		} catch (Exception e) {
			return null;
		}
	}
	public static Ability getNewAbilityFireImmunity()
	{
		try {
			return (Ability)Class.forName("morph.common.ability.AbilityHandler").getDeclaredMethod("getNewAbilityFireImmunity").invoke(null);
		} catch (Exception e) {
			return null;
		}
	}
	public static Ability getNewAbilityFloat(float terminalVelocity, boolean negateFallDamage)
	{
		try {
			return (Ability)Class.forName("morph.common.ability.AbilityHandler").getDeclaredMethod("getNewAbilityFloat", float.class, boolean.class).invoke(null, terminalVelocity, negateFallDamage);
		} catch (Exception e) {
			return null;
		}
	}
	public static Ability getNewAbilityFly(boolean slowdownInWater)
	{
		try {
			return (Ability)Class.forName("morph.common.ability.AbilityHandler").getDeclaredMethod("getNewAbilityFly", boolean.class).invoke(null, slowdownInWater);
		} catch (Exception e) {
			return null;
		}
	}
	public static Ability getNewAbilityFly()
	{
		try {
			return (Ability)Class.forName("morph.common.ability.AbilityHandler").getDeclaredMethod("getNewAbilityFly", boolean.class).invoke(null, true);
		} catch (Exception e) {
			return null;
		}
	}
	public static Ability getNewAbilityHostile()
	{
		try {
			return (Ability)Class.forName("morph.common.ability.AbilityHandler").getDeclaredMethod("getNewAbilityHostile").invoke(null);
		} catch (Exception e) {
			return null;
		}
	}
	public static Ability getNewAbilitySunburn()
	{
		try {
			return (Ability)Class.forName("morph.common.ability.AbilityHandler").getDeclaredMethod("getNewAbilitySunburn").invoke(null);
		} catch (Exception e) {
			return null;
		}
	}
	public static Ability getNewAbilitySwim(boolean canBreatheOnLand)
	{
		try {
			return (Ability)Class.forName("morph.common.ability.AbilityHandler").getDeclaredMethod("getNewAbilitySwim", boolean.class).invoke(null, canBreatheOnLand);
		} catch (Exception e) {
			return null;
		}
	}
	public static Ability getNewAbilityWaterAllergy()
	{
		try {
			return (Ability)Class.forName("morph.common.ability.AbilityHandler").getDeclaredMethod("getNewAbilityWaterAllergy").invoke(null);
		} catch (Exception e) {
			return null;
		}
	}

}
