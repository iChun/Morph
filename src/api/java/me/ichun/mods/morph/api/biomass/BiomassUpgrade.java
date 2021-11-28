package me.ichun.mods.morph.api.biomass;

import me.ichun.mods.morph.api.MorphApi;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;

public class BiomassUpgrade
{
    private String mobId;
    private String id; //TODO should mob traits start with mob_etc??
    private int level;

    @Nullable
    public BiomassUpgradeInfo upgradeInfo;

    public BiomassUpgrade(@Nullable String mobId, String id)
    {
        this.mobId = mobId;
        this.id = id;
        this.level = 0;
    }

    private BiomassUpgrade(){}

    public String getMobId()
    {
        return mobId;
    }

    public String getId()
    {
        return id;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int i)
    {
        level = i;
    }

    public double getValue()
    {
        updateUpgradeInfo();
        if(upgradeInfo != null && upgradeInfo.value != null)
        {
            return upgradeInfo.value.get(level);
        }
        return 0;
    }

    public void updateUpgradeInfo()
    {
        upgradeInfo = MorphApi.getApiImpl().getBiomassUpgradeInfo(mobId, id);
    }

    public CompoundNBT write(CompoundNBT tag)
    {
        if(mobId != null)
        {
            tag.putString("mobId", mobId);
        }
        tag.putString("id", id);
        tag.putInt("level", level);

        return tag;
    }

    public void read(CompoundNBT tag)
    {
        if(tag.contains("mobId"))
        {
            mobId = tag.getString("mobId");
        }
        id = tag.getString("id");
        level = tag.getInt("level");

        updateUpgradeInfo();
    }

    public static BiomassUpgrade createFromNBT(CompoundNBT tag)
    {
        BiomassUpgrade upgrade = new BiomassUpgrade();
        upgrade.read(tag);
        return upgrade;
    }
}
