package morph.common.core;

import java.lang.reflect.Method;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import morph.common.Morph;

public class ObfHelper 
{
	public static boolean obfuscation;
	
	public static final String[] mainModel			= new String[] { "i", "field_77045_g", "mainModel" 		}; //RendererLivingEntity
	public static final String[] textureOffsetX 	= new String[] { "r", "field_78803_o", "textureOffsetX" }; //ModelRenderer
	public static final String[] textureOffsetY 	= new String[] { "s", "field_78813_p", "textureOffsetY" }; //ModelRenderer
	public static final String[] resourceDomain		= new String[] { "a", "field_110626_a", "resourceDomain"}; //ResourceLocation
	public static final String[] resourcePath 		= new String[] { "b", "field_110625_b", "resourcePath" 	}; //ResourceLocation
	public static final String[] compiled 			= new String[] { "t", "field_78812_q", "compiled"	 	}; //ModelRenderer
	
	public static final String setSizeObf = "func_70105_a";
	public static final String setSizeDeobf = "setSize";

	public static final String updateEntityActionStateObf = "func_70626_be";
	public static final String updateEntityActionStateDeobf = "updateEntityActionState";
	
	public static final String getEntityTextureObf = "func_110775_a";
	
	public static final String preRenderCallbackObf = "func_77041_b";
	public static final String preRenderCallbackDeobf = "preRenderCallback";
	
	public static final String pushOutOfBlocksObf = "func_70048_i";
	public static final String pushOutOfBlocksDeobf = "pushOutOfBlocks";

	public static Method setSizeMethod;
	public static Method updateEntityActionStateMethod;
	public static Method pushOutOfBlocksMethod;
	
	public static void obfWarning()
	{
		Morph.console("Forgot to update obfuscation!", true);
	}
	
    public static void detectObfuscation()
    {
        try
        {
            Class.forName("net.minecraft.world.World");
            obfuscation = false;
        }
        catch (Exception e)
        {
            obfuscation = true;
        }

    }
	
	public static void forceSetSize(Entity ent, float width, float height)
	{
		if(setSizeMethod == null)
		{
			try
			{
				Method m = EntityLivingBase.class.getDeclaredMethod(ObfHelper.obfuscation ? ObfHelper.setSizeObf : ObfHelper.setSizeDeobf, float.class, float.class);
				setSizeMethod = m;
			}
			catch(NoSuchMethodException e)
			{
				ent.width = width;
				ent.height = height;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		if(setSizeMethod != null)
		{
			try
			{
				setSizeMethod.setAccessible(true);
				setSizeMethod.invoke(ent, width, height);				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void forceUpdateEntityActionState(EntityLivingBase ent)
	{
		if(updateEntityActionStateMethod == null)
		{
			try
			{
				Method m = EntityLivingBase.class.getDeclaredMethod(ObfHelper.obfuscation ? ObfHelper.updateEntityActionStateObf : ObfHelper.updateEntityActionStateDeobf);
				updateEntityActionStateMethod = m;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		if(updateEntityActionStateMethod != null)
		{
			try
			{
				updateEntityActionStateMethod.setAccessible(true);
				updateEntityActionStateMethod.invoke(ent);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static void forcePushOutOfBlocks(EntityPlayer ent, double d, double d1, double d2)
	{
		if(pushOutOfBlocksMethod == null)
		{
			try
			{
				Method m = EntityPlayerSP.class.getDeclaredMethod(ObfHelper.obfuscation ? ObfHelper.pushOutOfBlocksObf : ObfHelper.pushOutOfBlocksDeobf, double.class, double.class, double.class);
				pushOutOfBlocksMethod = m;
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		if(pushOutOfBlocksMethod != null)
		{
			try
			{
				pushOutOfBlocksMethod.setAccessible(true);
				pushOutOfBlocksMethod.invoke(ent, d, d1, d2);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static ResourceLocation invokeGetEntityTexture(Render rend, Class clz, EntityLivingBase ent)
	{
		try
		{
			Method m = clz.getDeclaredMethod(ObfHelper.getEntityTextureObf, Entity.class);
			m.setAccessible(true);
			return (ResourceLocation)m.invoke(rend, ent);
		}
		catch(NoSuchMethodException e)
		{
			if(clz != RendererLivingEntity.class)
			{
				return invokeGetEntityTexture(rend, clz.getSuperclass(), ent);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return AbstractClientPlayer.field_110314_b;
	}
	
	public static void invokePreRenderCallback(Render rend, Class clz, EntityLivingBase ent, float rendTick)
	{
		try
		{
			Method m = clz.getDeclaredMethod(ObfHelper.obfuscation ? ObfHelper.preRenderCallbackObf : ObfHelper.preRenderCallbackDeobf, EntityLivingBase.class, float.class);
			m.setAccessible(true);
			m.invoke(rend, ent, rendTick);
		}
		catch(NoSuchMethodException e)
		{
			if(clz != RendererLivingEntity.class)
			{
				invokePreRenderCallback(rend, clz.getSuperclass(), ent, rendTick);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	
}
