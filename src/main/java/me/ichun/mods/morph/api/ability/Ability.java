package me.ichun.mods.morph.api.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 *
 * Abstract ability class.
 * Think of it like the Entity class, extend it to make your own types.
 * Some abilities may seem more like traits/characteristic, but let's just call it an ability for simplicity's sake.
 * Please take note that entities that don't have an ability mapping will inherit their superclass' abilities.
 * @author iChun
 *
 */
public abstract class Ability
{
    /**
     * Ability parent field. Will be null for instances used in registration. Ability is then cloned and parent assigned later on.
     */
    private transient EntityLivingBase parent;

    /**
     * This value gets set from 0.0F to 1.0F as the player morphs, 1.0F to 0.0F as the player demorphs.
     * Both morph states may have the same ability but will have different ability objects, so the strength value is important?? Still WIP.
     */
    public transient float strength;

    //TODO transition between two abilities? Change in strength as player demorphs/morphs?

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
     * @param ent new parent that this ability clone attaches to
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
     * Rendering to be done post-render.
     * EG: Used by AbilitySwim to render air bubbles whilst on land.
     */
    @SideOnly(Side.CLIENT)
    public abstract void postRender();

    /**
     * Icon location for ability. Can be null.
     * Morph's default icons are 32x32. Can be any resolution though.
     * @return resourcelocation for icon
     */
    @SideOnly(Side.CLIENT)
    public abstract ResourceLocation getIcon();

    /**
     * Does the entity have the ability?
     * @param living living entity to test this on
     * @return entity has the ability
     */
    @SideOnly(Side.CLIENT)
    public boolean entityHasAbility(EntityLivingBase living)
    {
        return true;
    }

    /**
     * Is the ability a characteristic of the entity? Characteristics will immediately be with the morph and will not be learnt over time.
     * @param living living entity to test this on.
     * @return is the ability a characteristic
     */
    public boolean isAbilityCharacteristic(EntityLivingBase living) { return false; }

    private static IAbilityHandler abilityHandlerImpl = new AbilityHandlerDummy();

    /**
     * Registers the ability so the mod can look up the class when attempting to load Ability save data.
     * Call this no later than PostInit.
     * @param name Ability type/name
     * @param clz AbilityClass
     */
    public static void registerAbility(String name, Class<? extends Ability> clz)
    {
        abilityHandlerImpl.registerAbility(name, clz);
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
        abilityHandlerImpl.mapAbilities(entClass, abilities);
    }

    /**
     * Superman's kryptonite.
     * @param entClass Entity class to remove ability from
     * @param type Ability type
     */
    public static void removeAbility(Class<? extends EntityLivingBase> entClass, String type)
    {
        abilityHandlerImpl.removeAbility(entClass, type);
    }

    /**
     * Checks to see if the entity class has a mapped ability type.
     * @param entClass
     * @param type Ability type
     * @return Entity class has ability type
     */
    public static boolean hasAbility(Class<? extends EntityLivingBase> entClass, String type)
    {
        return abilityHandlerImpl.hasAbility(entClass, type);
    }

    /**
     * Creates an ability by type.
     * Check out AbilityHandler to see each Ability type and the parse function in their respective classes for the arguments.
     * @return
     */
    public static Ability createNewAbilityByType(String type, String...arguments)
    {
        return abilityHandlerImpl.createNewAbilityByType(type, arguments);
    }

    /**
     * Get the IAbilityHandler implementation for Morph.
     * @return returns the IAbilityHandler implementation from morph. May be the AbilityHandlerDummy if Morph has not loaded.
     */
    public static IAbilityHandler getAbilityHandlerImpl()
    {
        return abilityHandlerImpl;
    }

    /**
     * Sets the IAbilityHandler implementation for Morph.
     * For use of Morph, so please don't actually use this.
     * @param abilityHandlerImpl API implementation to set.
     */
    public static void setAbilityHandlerImpl(IAbilityHandler abilityHandlerImpl)
    {
        Ability.abilityHandlerImpl = abilityHandlerImpl;
    }

}
