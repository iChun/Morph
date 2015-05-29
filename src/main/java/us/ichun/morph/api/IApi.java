package us.ichun.morph.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

public interface IApi
{
    /**
     * Returns if a player is has a morph. If morph progress < 1.0F, player is mid-morphing.
     * Players demorphing are considered as a player with a morph until the demorph is complete.
     * Players demorphing will have their target morph being a player entity with their name/UUID
     * @param playerName Username
     * @param isClient (false for Serverside)
     */
    public boolean hasMorph(String playerName, boolean isClient);

    /**
     * Returns morph progression of a player. Get the time for a morph to complete from the timeToCompleteMorph function.
     * If player does not have a morph, 1.0F will be returned.
     * @param playerName Username
     * @param isClient (false for Serverside)
     */
    public float morphProgress(String playerName, boolean isClient);

    /**
     * Return the time needed for a morph to complete when starting a morph.
     * @return Time (in ticks) needed to complete a morph. Default/no impl will be 80 ticks.
     */
    public float timeToCompleteMorph();

    /**
     * Returns previous entity instance used to render the morph.
     * If player does not have a previous morph state or a morph, null will be returned.
     * @param playerName Username
     * @param isClient (false for Serverside)
     */
    public EntityLivingBase getPrevMorphEntity(String playerName, boolean isClient);

    /**
     * Returns entity instance used to render the morph.
     * If player does not have a morph, null will be returned.
     * @param playerName Username
     * @param isClient (false for Serverside)
     */
    public EntityLivingBase getMorphEntity(String playerName, boolean isClient);

    /**
     * Forces a player to demorph.
     * Called Serverside only.
     * @param player Player to force to demorph
     */
    public void forceDemorph(EntityPlayerMP player);

    /**
     * Returns true if the Api Implementation is actually Morph's.
     * The ApiDummy returns false.
     * @return Is this Implementation the actual Morph API.
     */
    public boolean isMorphApi();
}
