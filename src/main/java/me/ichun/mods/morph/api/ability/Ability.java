package me.ichun.mods.morph.api.ability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

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
        implements Comparable<Ability>
{

    /**
     * Ability parent field. Will be null for instances used in registration. Ability is then cloned and parent assigned later on.
     */
    private transient EntityLivingBase parent;

    /**
     * ResourceLocation for this ability's icon. Can be null.
     */
    protected transient ResourceLocation iconResource;

    /**
     * The current strength of the ability. Set by the progression of the morph.
     */
    public transient float strength = 1.0F;

    /**
     * Ability type in String - Used for creation of the ability from the JSON file.
     */
    public String type = "unknown";

    /**
     * Is the ability an "active" type ability? Active type abilities are usable/triggerable on demand.
     */
    public Boolean activeType;

    /**
     * Cost of using the ability, per use/tick
     */
    public Float activeCost;

    /**
     * Is the ability a characteristic of the Morph and does not need to be learnt?
     */
    public Boolean notCharacteristic;

    /**
     * Basic constructor (but you didn't really need me to tell you that ;D )
     */
    public Ability()
    {
        parent = null;
        iconResource = null;
    }

    /**
     * Function for mod mob support, with args.
     */
    public Ability parse(String[] args) { return this; }

    //TEMP
    public Ability setNotCharacteristic()
    {
        notCharacteristic = true;
        return this;
    }

    /**
     * Each ability has to return a String type.
     * This is used for comparison, saving, as well as construction/loading of Ability.
     * Think of it like the way Minecraft registers entities.
     * @return Ability type
     */
    public String getType()
    {
        return type;
    }

    /**
     * Creates a copy of this ability for use with parents.
     * As previously stated before the ability instance used during registration is a base so it needs to be cloned for use with parents.
     */
    public Ability clone()
    {
        Ability ability = AbilityApi.GSON.fromJson(AbilityApi.GSON.toJson(this), this.getClass());
        return ability;
    }

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
     * Ticks every world tick, basically an ability onUpdate, similar to Entity's onUpdate.
     * Will only tick if getParent() is not null.
     * Please remember that getParent is not necessarily a player.
     */
    public void tick(){}

    /**
     * Called when the ability is assigned a new parent.
     */
    public void init(){}

    /**
     * Called when the ability is finally removed when the parent demorphs or morphs into a state that does not have this ability type.
     * This will NOT be called if the parent morphs into another morph that has this type of ability.
     * The next abilities list is there to handle other abilities present that may be similar but not of the same kind, eg: AbilityFlightFlap and AbilityFlightHover
     */
    public void kill(ArrayList<Ability> nextAbilities){}

    /**
     * Gets the strength of the ability from 0F to 1F;
     */
    public float getStrength()
    {
        return strength;
    }

    /**
     * Does the entity have the ability?
     * @param living living entity to test this on
     * @return entity has the ability
     */
    public boolean entityHasAbility(EntityLivingBase living)
    {
        return true;
    }

    /**
     * Return true if the ability is currently active, eg when the climb ability lets you climb up walls
     * @return is ability active
     */
    public boolean isActiveType()
    {
        return activeType != null && activeType;
    }

    /**
     * Cost of using an ability, per use/tick.
     * @return ability cost
     */
    public float activeCost()
    {
        return activeCost != null ? activeCost : 0F;
    }

    /**
     * Is the ability a characteristic of the entity? Characteristics will immediately be with the morph and will not be learnt over time.
     * @param living living entity to test this on.
     * @return is the ability a characteristic
     */
    public boolean isCharacteristic(EntityLivingBase living) { return !(notCharacteristic != null && notCharacteristic); }

    /**
     * Icon location for ability. Can be null.
     * Morph's default icons are 32x32. Can be any resolution though.
     * @return resourcelocation for icon
     */
    @SideOnly(Side.CLIENT)
    public ResourceLocation getIcon()
    {
        return iconResource;
    }

    /**
     * Rendering to be done post-render.
     * EG: Used by AbilitySwim to render air bubbles whilst on land.
     */
    @SideOnly(Side.CLIENT)
    public void postRender(){}

    @Override
    public int compareTo(Ability ability)
    {
        return getType().compareTo(ability.getType());
    }
}
