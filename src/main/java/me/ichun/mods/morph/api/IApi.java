package me.ichun.mods.morph.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.relauncher.Side;

public interface IApi
{
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
     * @param playerName Username
     * @param side Client or Server side?
     */
    public EntityLivingBase getPrevMorphEntity(String playerName, Side side);

    /**
     * Returns entity instance used to render the morph.
     * If player does not have a morph, null will be returned.
     * @param playerName Username
     * @param side Client or Server side?
     */
    public EntityLivingBase getMorphEntity(String playerName, Side side);

    /**
     * Forces a player to demorph.
     * Called Serverside only.
     * @param player Player to force to demorph
     */
    public void forceDemorph(EntityPlayerMP player);

    /**
     * Forces a player to morph into the provided entity.
     * Called Serverside only.
     * @param player Player to force to morph
     * @param entityToMorph Entity to force the player to morph into
     */
    public void forceMorph(EntityPlayerMP player, EntityLivingBase entityToMorph);

    /**
     * Forces a player to acquire the provided entity.
     * Called Serverside only.
     * @param player Player to force to morph
     * @param entityToAcquire Entity to force the player to acquire
     */
    public void acquireMorph(EntityPlayerMP player, EntityLivingBase entityToAcquire);

    /**
     * Returns true if the Api Implementation is actually Morph's.
     * The ApiDummy returns false.
     * @return Is this Implementation the actual Morph API.
     */
    public boolean isMorphApi();
}
