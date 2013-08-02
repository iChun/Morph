package morph.common.core;

import java.lang.reflect.Method;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import morph.common.Morph;

public class ObfHelper 
{
	public static boolean obfuscation;
	
	public static final String[] mainModel			= new String[] { "i", "field_77045_g", "mainModel" 		}; //RendererLivingEntity
	public static final String[] textureOffsetX 	= new String[] { "r", "field_78803_o", "textureOffsetX" }; //ModelRenderer
	public static final String[] textureOffsetY 	= new String[] { "s", "field_78813_p", "textureOffsetY" }; //ModelRenderer
	
	public static final String setSizeObf = "func_70105_a";
	public static final String setSizeDeobf = "setSize";
	
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
		try
		{
			Method m = EntityLivingBase.class.getDeclaredMethod(ObfHelper.obfuscation ? ObfHelper.setSizeObf : ObfHelper.setSizeDeobf, float.class, float.class);
			m.setAccessible(true);
			m.invoke(ent, width, height);
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
}
