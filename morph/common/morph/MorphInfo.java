package morph.common.morph;

import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.EntityLivingBase;

public class MorphInfo 
{
	public Class prevEntClass;
	public EntityLivingBase prevEntInstance;
	public DataWatcher prevEntDataWatcher;
	
	public Class nextEntClass;
	public EntityLivingBase nextEntInstance;
	public DataWatcher nextEntDataWatcher;
	
	private boolean morphing; //if true, increase progress
	public int morphProgress; //up to 80, 3 sec sound files, 0.5 sec between sounds where the skin turns black
	
	public void setMorphing(boolean flag)
	{
		morphing = flag;
	}
	
	public boolean getMorphing()
	{
		return morphing;
	}
}
