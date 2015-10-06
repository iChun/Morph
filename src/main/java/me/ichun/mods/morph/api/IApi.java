package me.ichun.mods.morph.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IApi
{
    /**
     * Returns if a player can morph.
     * @param player player instance
     * @return if said player can morph.
     */
    public boolean canPlayerMorph(EntityPlayer player);

    /**
     * Returns if a player is has a morph. If morph progress < 1.0F, player is mid-morphing.
     * Players demorphing are considered as a player with a morph until the demorph is complete.
     * Players demorphing will have their target morph being a player entity with their name/UUID
     * @param playerName Username
     * @param side Client or Server side?
     */
    public boolean hasMorph(String playerName, Side side);

    /**
     * Returns morph progression of a player. Get the time for a morph to complete from the timeToCompleteMorph function.
     * If player does not have a morph, 1.0F will be returned.
     * @param playerName Username
     * @param side Client or Server side?
     */
    public float morphProgress(String playerName, Side side);

    /**
     * Return the time needed for a morph to complete when starting a morph.
     * @return Time (in ticks) needed to complete a morph. Default/no impl will be 80 ticks.
     */
    public float timeToCompleteMorph();

    /**
     * Returns previous entity instance used to render the morph.
     * If player does not have a previous morph state or a morph, null will be returned.
     * @param worldInstance World instance to create the entity for. Ensure it's for the correct side.
     * @param playerName Username
     * @param side Client or Server side?
     */
    public EntityLivingBase getPrevMorphEntity(World worldInstance, String playerName, Side side);

    /**
     * Returns entity instance used to render the morph.
     * If player does not have a morph, null will be returned.
     * @param worldInstance World instance to create the entity for. Ensure it's for the correct side.
     * @param playerName Username
     * @param side Client or Server side?
     */
    public EntityLivingBase getMorphEntity(World worldInstance, String playerName, Side side);

    /**
     * Forces a player to demorph. May return false if called while player is mid-morphing
     * Called Serverside only.
     * @param player Player to force to demorph
     */
    public boolean forceDemorph(EntityPlayerMP player);

    /**
     * Forces a player to morph into the provided entity without acquiring the morph. May return false if called while player is mid-morphing
     * Called Serverside only.
     * @param player Player to force to morph
     * @param entityToMorph Entity to force the player to morph into
     * @return if forcing the morph was successful
     */
    public boolean forceMorph(EntityPlayerMP player, EntityLivingBase entityToMorph);

    /**
     * Forces a player to acquire the provided entity. May return false if called while player is mid-morphing
     * Called Serverside only.
     * @param player Player to force to morph
     * @param entityToAcquire Entity to force the player to acquire
     * @param forceMorph Force the player to morph into this entity?
     * @param killEntityClientside Kill the entity on the client and spawn the "morph acquired" effect?
     * @return if acquiring the morph (and morphing into it, if true) was successful
     */
    public boolean acquireMorph(EntityPlayerMP player, EntityLivingBase entityToAcquire, boolean forceMorph, boolean killEntityClientside);

    public ResourceLocation getMorphSkinTexture();

    /**
     * Renders the "arm" (if exists) of the player if the player is morphed..
     * Morph will position the arm in a similar way as how it's done with RenderPlayer's renderLeft/RightArm.
     * @param player EntityPlayer's arm to render
     * @param isLeftArm Is the arm supposed to be the left arm?
     */
    @SideOnly(Side.CLIENT)
    public void renderArm(EntityPlayer player, boolean isLeftArm);

    /**
     * Returns true if the Api Implementation is actually Morph's.
     * The ApiDummy returns false.
     * @return Is this Implementation the actual Morph API.
     */
    public boolean isMorphApi();
}
