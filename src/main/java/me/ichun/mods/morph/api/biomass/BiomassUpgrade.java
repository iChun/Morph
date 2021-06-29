package me.ichun.mods.morph.api.biomass;

import me.ichun.mods.morph.api.MorphApi;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;

public class BiomassUpgrade
{
    public String id;
    public int level;

    @Nullable
    public BiomassUpgradeInfo upgradeInfo;

    public BiomassUpgrade(String id)
    {
        this.id = id;
        this.level = 0;
    }

    private BiomassUpgrade(){}

    public double getValue()
    {
        if(upgradeInfo != null)
        {
            if(upgradeInfo.valueMultiplier != null)
            {
                return upgradeInfo.valueMultiplier.apply(upgradeInfo.baseValue, level);
            }

            return upgradeInfo.baseValue;
        }
        return 0;
    }

    public CompoundNBT write(CompoundNBT tag)
    {
        tag.putString("id", id);
        tag.putInt("level", level);

        return tag;
    }

    public void read(CompoundNBT tag)
    {
        id = tag.getString("id");
        level = tag.getInt("level");

        upgradeInfo = MorphApi.getApiImpl().getBiomassUpgradeInfo(id);
    }

    public static BiomassUpgrade createFromNBT(CompoundNBT tag)
    {
        BiomassUpgrade upgrade = new BiomassUpgrade();
        upgrade.read(tag);
        return upgrade;
    }
}
