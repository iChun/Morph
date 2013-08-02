package morph.common.core;

import morph.common.Morph;

public class ObfHelper 
{
	public static final String[] mainModel			= new String[] { "i", "field_77045_g", "mainModel" 		}; //RendererLivingEntity
	public static final String[] textureOffsetX 	= new String[] { "r", "field_78803_o", "textureOffsetX" }; //ModelRenderer
	public static final String[] textureOffsetY 	= new String[] { "s", "field_78813_p", "textureOffsetY" }; //ModelRenderer
	
	public static void obfWarning()
	{
		Morph.console("Forgot to update obfuscation!", true);
	}
}
