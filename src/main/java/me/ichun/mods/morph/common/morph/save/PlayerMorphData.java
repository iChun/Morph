package me.ichun.mods.morph.common.morph.save;

import me.ichun.mods.morph.api.biomass.BiomassUpgrade;
import me.ichun.mods.morph.api.morph.MorphVariant;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.UUID;

public class PlayerMorphData
{
    public UUID owner;
    public ArrayList<MorphVariant> morphs;
    public double biomass;
    public ArrayList<BiomassUpgrade> upgrades;

    public PlayerMorphData()
    {
        this.morphs = new ArrayList<>();
        this.upgrades = new ArrayList<>();
    }

    public PlayerMorphData(UUID owner)
    {
        this.owner = owner;

        //Add the player's default morph
        this.morphs = new ArrayList<>();
        MorphVariant variant = MorphVariant.createPlayerMorph(owner, false);
        variant.variants.get(0).identifier = MorphVariant.IDENTIFIER_DEFAULT_PLAYER_STATE;
        this.morphs.add(variant); //add the player as a morph, this should never be deleted.

        //add the first level of the capacity upgrade
        this.upgrades = new ArrayList<>();
        String[] defaultUpgrades = new String[] { "biomass_capacity", "biomass_efficiency", "biomass_reach" };
        for(String id : defaultUpgrades)
        {
            BiomassUpgrade upgrade = new BiomassUpgrade(null, id);
            upgrade.setLevel(1);
            this.upgrades.add(upgrade);
        }
    }

    public boolean containsVariant(MorphVariant variant)
    {
        for(MorphVariant morph : morphs)
        {
            if(morph.id.equals(variant.id))
            {
                return morph.containsVariant(variant);
            }
        }
        return false;
    }

    public MorphVariant addVariant(MorphVariant variant) //returns the morph it was added to.
    {
        for(MorphVariant morph : morphs)
        {
            if(morph.combineVariants(variant))
            {
                return morph;
            }
        }

        //it wasn't added, add it.
        MorphVariant varClone = MorphVariant.createFromNBT(variant.write(new CompoundNBT()));

        varClone.variants.add(variant.thisVariant);
        varClone.thisVariant = null;

        morphs.add(varClone);
        return varClone;
    }

    @Nullable
    public BiomassUpgrade getBiomassUpgrade(String id)
    {
        for(BiomassUpgrade upgrade : upgrades)
        {
            if(upgrade.getId().equals(id))
            {
                return upgrade;
            }
        }
        return null;
    }

    public double getBiomassUpgradeValue(String id)
    {
        BiomassUpgrade biomassUpgrade = getBiomassUpgrade(id);
        if(biomassUpgrade != null)
        {
            return biomassUpgrade.getValue();
        }
        return 0D;
    }

    public CompoundNBT write(CompoundNBT tag)
    {
        tag.putUniqueId("owner", owner);
        tag.putInt("morphCount", morphs.size());
        for(int i = 0; i < morphs.size(); i++)
        {
            tag.put("morph_" + i, morphs.get(i).write(new CompoundNBT()));
        }

        tag.putDouble("biomass", biomass);

        tag.putInt("upgradeCount", upgrades.size());
        for(int i = 0; i < upgrades.size(); i++)
        {
            tag.put("upgrade_" + i, upgrades.get(i).write(new CompoundNBT()));
        }

        return tag;
    }

    public void read(CompoundNBT tag)
    {
        owner = tag.getUniqueId("owner");

        morphs.clear();
        int count = tag.getInt("morphCount");
        for(int i = 0; i < count; i++)
        {
            morphs.add(MorphVariant.createFromNBT(tag.getCompound("morph_" + i)));
        }

        biomass = tag.getDouble("biomass");

        upgrades.clear();
        count = tag.getInt("upgradeCount");
        for(int i = 0; i < count; i++)
        {
            upgrades.add(BiomassUpgrade.createFromNBT(tag.getCompound("upgrade_" + i)));
        }
    }
}
