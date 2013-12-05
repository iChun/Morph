package morph.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

public final class Api 
{
	/**
	 * Returns if a player is has a morph. If morph progress < 1.0F, player is mid-morphing.
	 * Players demorphing are considered as a player with a morph until the demorph is complete.
	 * @param Player Username
	 * @param Clientside (false for Serverside)
	 */
	public static boolean hasMorph(String playerName, boolean isClient)
	{
		try {
			return (Boolean)Class.forName("morph.common.core.ApiHandler").getDeclaredMethod("hasMorph", String.class, boolean.class).invoke(null, playerName, isClient);
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Returns morph progression of a player. Time per morph is 80 ticks.
	 * If player does not have a morph, 1.0F will be returned.
	 * @param Player Username
	 * @param Clientside (false for Serverside)
	 */
	public static float morphProgress(String playerName, boolean isClient)
	{
		try {
			return (Float)Class.forName("morph.common.core.ApiHandler").getDeclaredMethod("morphProgress", String.class, boolean.class).invoke(null, playerName, isClient);
		} catch (Exception e) {
			return 1.0F;
		}
	}
	
	/**
	 * Returns entity instance used to render the morph.
	 * If player does not have a morph, null will be returned.
	 * @param Player Username
	 * @param Clientside (false for Serverside)
	 */
	public static EntityLivingBase getMorphEntity(String playerName, boolean isClient)
	{
		try {
			return (EntityLivingBase)Class.forName("morph.common.core.ApiHandler").getDeclaredMethod("getMorphEntity", String.class, boolean.class).invoke(null, playerName, isClient);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Blacklists an entity from being morphed into.
	 * Previously saved morphs of the classtype will not be removed.
	 * @param Class (extends EntityLivingBase)
	 */
	public static void blacklistEntity(Class<? extends EntityLivingBase> clz)
	{
		try {
			Class.forName("morph.common.core.ApiHandler").getDeclaredMethod("blacklistEntity", Class.class).invoke(null, clz);
		} catch (Exception e) {
		}
	}
	
	/**
	 * Forces a player to morph into an EntityLivingBase, also adds said entity to the morph list.
	 * Called Serverside only.
	 * @param player
	 * @param living
	 * @return morphSuccessful
	 */
	public static boolean forceMorph(EntityPlayerMP player, EntityLivingBase living)
	{
		try {
			return (Boolean)Class.forName("morph.common.core.ApiHandler").getDeclaredMethod("forceMorph", EntityPlayerMP.class, EntityLivingBase.class).invoke(null, player, living);
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Forces a player to demorph.
	 * Called Serverside only.
	 * @param player
	 */
	public static void forceDemorph(EntityPlayerMP player)
	{
		try {
			Class.forName("morph.common.core.ApiHandler").getDeclaredMethod("forceDemorph", EntityPlayerMP.class).invoke(null, player);
		} catch (Exception e) {
		}
	}
	
}
