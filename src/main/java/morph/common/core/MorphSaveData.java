package morph.common.core;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

public class MorphSaveData extends WorldSavedData
{
    public boolean hasTravelledToNether;
    public boolean hasKilledWither;

    public MorphSaveData(String par1Str)
    {
        super(par1Str);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag)
    {
        hasTravelledToNether = tag.getBoolean("hasTravelledToNether");
        hasKilledWither = tag.getBoolean("hasKilledWither");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag)
    {
        tag.setBoolean("hasTravelledToNether", hasTravelledToNether);
        tag.setBoolean("hasKilledWither", hasKilledWither);
    }
}
